package com.iamasaw.privacy.core.data.repository.defaults

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.common.time.Clock
import com.iamasaw.privacy.core.data.mapper.MessagePayloadMapper
import com.iamasaw.privacy.core.data.mapper.mergeExisting
import com.iamasaw.privacy.core.data.mapper.runCatchingApp
import com.iamasaw.privacy.core.data.mapper.toEntity
import com.iamasaw.privacy.core.data.mapper.toEpochMicros
import com.iamasaw.privacy.core.data.mapper.toInstantFromMicros
import com.iamasaw.privacy.core.data.mapper.toMessage
import com.iamasaw.privacy.core.data.repository.CurrentChatUser
import com.iamasaw.privacy.core.data.repository.MessageRepository
import com.iamasaw.privacy.core.data.repository.OutboxScheduler
import com.iamasaw.privacy.core.data.repository.SendPendingResult
import com.iamasaw.privacy.core.data.repository.chatTransaction
import com.iamasaw.privacy.core.data.repository.isPermanentSendStatus
import com.iamasaw.privacy.core.database.MessengerDatabase
import com.iamasaw.privacy.core.database.entity.MessageEntity
import com.iamasaw.privacy.core.database.entity.SyncCursorEntity
import com.iamasaw.privacy.core.model.ConversationId
import com.iamasaw.privacy.core.model.Message
import com.iamasaw.privacy.core.model.MessageId
import com.iamasaw.privacy.core.model.MessageStatus
import com.iamasaw.privacy.core.model.UserId
import com.iamasaw.privacy.core.network.conversation.ConversationRemoteDataSource
import com.iamasaw.privacy.core.network.dto.MessageCursorDto
import com.iamasaw.privacy.core.network.message.MessageRemoteDataSource
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Optional
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Suppress("LongParameterList")
class DefaultMessageRepository
    @Inject
    internal constructor(
        private val database: MessengerDatabase,
        private val remote: MessageRemoteDataSource,
        private val conversations: ConversationRemoteDataSource,
        private val currentUser: CurrentChatUser,
        private val clock: Clock,
        private val payloads: MessagePayloadMapper,
        private val scheduler: Optional<OutboxScheduler>,
    ) : MessageRepository {
        private val messages = database.messageDao()
        private val conversationDao = database.conversationDao()
        private val cursors = database.syncCursorDao()
        private val inboxMutex = Mutex()

        override fun observeMessages(conversationId: ConversationId): Flow<PagingData<Message>> =
            Pager(
                config =
                    PagingConfig(
                        pageSize = PAGE_SIZE,
                        enablePlaceholders = false,
                    ),
                pagingSourceFactory = {
                    messages.pagingSource(conversationId.value)
                },
            ).flow.map { page ->
                page.map { it.toMessage() }
            }

        override suspend fun enqueueOutgoing(
            conversationId: ConversationId,
            text: String,
        ): AppResult<MessageId> =
            runCatchingApp {
                require(text.isNotBlank()) {
                    "Message text is blank"
                }

                val owner = currentUser.requireId()
                val id = MessageId(UUID.randomUUID().toString())

                database.chatTransaction {
                    currentUser.check(owner)

                    checkNotNull(conversationDao.getById(conversationId.value)) {
                        "Conversation is not cached"
                    }

                    check(
                        messages.insert(
                            MessageEntity(
                                id = id.value,
                                conversationId = conversationId.value,
                                senderId = owner.value,
                                text = text,
                                localCreatedAtMicros = clock.now().toEpochMicros(),
                            ),
                        ) != -1L,
                    ) {
                        "Message ID already exists"
                    }
                }

                val scheduling =
                    runCatchingApp {
                        scheduler.ifPresent {
                            it.schedule(conversationId)
                        }
                    }

                if (scheduling is AppResult.Failure) {
                    Log.w(
                        "MessageRepository",
                        "Message saved; outbox scheduling needs retry",
                    )
                }

                id
            }

        override suspend fun sendPending(messageId: MessageId): SendPendingResult {
            var permanentlyFailed = false

            val attempt =
                runCatchingApp {
                    val owner = currentUser.requireId()
                    val row = messages.getById(messageId.value) ?: return@runCatchingApp

                    require(row.senderId == owner.value) {
                        "Message belongs to another person"
                    }

                    if (row.status != MessageStatus.PENDING.name) {
                        return@runCatchingApp
                    }

                    currentUser.check(owner)

                    val response =
                        try {
                            remote.insert(payloads.outgoing(row))
                        } catch (error: RestException) {
                            if (isPermanentSendStatus(error.statusCode)) {
                                markFailed(row, owner)
                                permanentlyFailed = true
                            }
                            throw error
                        } catch (error: IllegalArgumentException) {
                            markFailed(row, owner)
                            permanentlyFailed = true
                            throw error
                        }

                    check(
                        response.id == row.id && response.conversationId == row.conversationId,
                    )
                    check(response.senderId == owner.value)

                    val acknowledged = payloads.incoming(response, owner)

                    database.chatTransaction {
                        currentUser.check(owner)

                        val existing = messages.getById(row.id) ?: return@chatTransaction

                        messages.upsert(
                            acknowledged.mergeExisting(existing),
                        )
                    }
                }

            return when (attempt) {
                is AppResult.Success -> {
                    SendPendingResult.Complete
                }

                is AppResult.Failure -> {
                    if (permanentlyFailed) {
                        SendPendingResult.Failed(attempt.error)
                    } else {
                        SendPendingResult.Retry(attempt.error)
                    }
                }
            }
        }

        override suspend fun syncInbox(): AppResult<Unit> =
            inboxMutex.withLock {
                runCatchingApp {
                    val owner = currentUser.requireId()
                    refreshConversations(owner)

                    var cursor =
                        cursors.getByUserId(owner.value)?.let {
                            MessageCursorDto(
                                createdAt = it.createdAtMicros.toInstantFromMicros().toString(),
                                id = it.messageId,
                            )
                        }

                    do {
                        currentUser.check(owner)

                        val page = remote.fetchSince(cursor, PAGE_SIZE)
                        if (page.isEmpty()) break

                        val decoded =
                            page.map {
                                payloads.incoming(it, owner)
                            }

                        val missingConversations =
                            decoded.any {
                                conversationDao.getById(it.conversationId) == null
                            }

                        if (missingConversations) {
                            refreshConversations(owner)
                        }

                        database.chatTransaction {
                            currentUser.check(owner)

                            decoded.forEach { incoming ->
                                val existing = messages.getById(incoming.id)
                                messages.upsert(
                                    incoming.mergeExisting(existing),
                                )
                            }

                            val last = decoded.last()

                            cursors.upsert(
                                SyncCursorEntity(
                                    userId = owner.value,
                                    createdAtMicros = checkNotNull(last.serverCreatedAtMicros),
                                    messageId = last.id,
                                ),
                            )
                        }

                        val last = page.last()
                        cursor =
                            MessageCursorDto(
                                createdAt = last.createdAt,
                                id = last.id,
                            )
                    } while (page.size == PAGE_SIZE)
                }
            }

        private suspend fun refreshConversations(owner: UserId) {
            val rows =
                conversations.fetchMyConversations().map {
                    it.toEntity(owner)
                }

            database.chatTransaction {
                currentUser.check(owner)
                rows.forEach {
                    conversationDao.upsert(it)
                }
            }
        }

        private suspend fun markFailed(
            row: MessageEntity,
            owner: UserId,
        ) {
            database.chatTransaction {
                currentUser.check(owner)

                val existing = messages.getById(row.id)
                if (existing?.status == MessageStatus.PENDING.name) {
                    messages.upsert(
                        existing.copy(status = MessageStatus.FAILED.name),
                    )
                }
            }
        }

        private companion object {
            const val PAGE_SIZE = 100
        }
    }
