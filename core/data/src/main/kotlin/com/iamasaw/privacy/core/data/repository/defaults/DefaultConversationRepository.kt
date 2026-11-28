package com.iamasaw.privacy.core.data.repository.defaults

import android.os.Messenger
import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.data.mapper.runCatchingApp
import com.iamasaw.privacy.core.data.mapper.toConversation
import com.iamasaw.privacy.core.data.mapper.toEntity
import com.iamasaw.privacy.core.data.repository.ConversationRepository
import com.iamasaw.privacy.core.data.repository.CurrentChatUser
import com.iamasaw.privacy.core.data.repository.chatTransaction
import com.iamasaw.privacy.core.database.MessengerDatabase
import com.iamasaw.privacy.core.model.Conversation
import com.iamasaw.privacy.core.model.ConversationId
import com.iamasaw.privacy.core.model.UserId
import com.iamasaw.privacy.core.network.conversation.ConversationRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultConversationRepository
    @Inject
    internal constructor(
        private val database: MessengerDatabase,
        private val remote: ConversationRemoteDataSource,
        private val currentUser: CurrentChatUser,
    ) : ConversationRepository {
        private val conversations = database.conversationDao()

        override fun observeAll(): Flow<List<Conversation>> =
            conversations.observeAll().map { rows ->
                rows.map { it.toConversation() }
            }

        override suspend fun startDirectChat(otherUserId: UserId): AppResult<ConversationId> =
            runCatchingApp {
                val owner = currentUser.requireId()

                require(otherUserId != owner) {
                    "Cannot start a direct chat with yourself"
                }

                val id = remote.startDirectChat(otherUserId.value)

                currentUser.check(owner)

                val dto =
                    remote.fetchMyConversations().single {
                        it.id == id
                    }

                val row = dto.toEntity(owner)

                check(row.peerUserId == otherUserId.value) {
                    "Unexpected conversation peer"
                }

                database.chatTransaction {
                    currentUser.check(owner)
                    conversations.upsert(row)
                }

                ConversationId(id)
            }
    }
