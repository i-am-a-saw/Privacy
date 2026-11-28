package com.iamasaw.privacy.core.data.repository

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.iamasaw.privacy.core.common.error.AppError
import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.data.repository.defaults.DefaultConversationRepository
import com.iamasaw.privacy.core.database.MessengerDatabase
import com.iamasaw.privacy.core.database.entity.MessageEntity
import com.iamasaw.privacy.core.model.ConversationId
import com.iamasaw.privacy.core.model.MessageStatus
import com.iamasaw.privacy.core.model.UserId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class DefaultConversationRepositoryTest {
    private lateinit var database: MessengerDatabase
    private lateinit var remote: FakeConversationRemote
    private lateinit var repository: ConversationRepository

    @Before
    fun setUp() =
        runBlocking {
            val context =
                InstrumentationRegistry.getInstrumentation().targetContext

            database =
                Room
                    .inMemoryDatabaseBuilder<MessengerDatabase>(context)
                    .setDriver(BundledSQLiteDriver())
                    .setQueryCoroutineContext(Dispatchers.IO)
                    .build()

            remote = FakeConversationRemote()
            repository =
                DefaultConversationRepository(
                    database,
                    remote,
                    readyChatUser(),
                )
        }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun startedChatIsCachedAndObservationIncludesItsMessages() =
        runBlocking {
            assertEquals(
                AppResult.Success(ConversationId("conversation-1")),
                repository.startDirectChat(UserId("bob")),
            )

            assertNotNull(
                database.conversationDao().getById("conversation-1"),
            )

            database.messageDao().insert(
                MessageEntity(
                    id = "message-1",
                    conversationId = "conversation-1",
                    senderId = "bob",
                    text = "Hello Alice",
                    localCreatedAtMicros = 1_000_000L,
                    serverCreatedAtMicros = 2_000_001L,
                    status = MessageStatus.SENT.name,
                    isUnread = true,
                ),
            )

            val conversation = repository.observeAll().first().single()

            assertEquals(UserId("bob"), conversation.peer.id)
            assertEquals("Bob", conversation.peer.displayName)
            assertEquals("Hello Alice", conversation.lastMessagePreview)
            assertEquals(
                Instant.parse("1970-01-01T00:00:02.000001Z"),
                conversation.lastMessageAt,
            )
            assertEquals(1, conversation.unreadCount)
        }

    @Test
    fun repeatedStartUpdatesPeerWithoutDuplicatingConversation() =
        runBlocking {
            assertEquals(
                AppResult.Success(ConversationId("conversation-1")),
                repository.startDirectChat(UserId("bob")),
            )

            remote.peerName = "Bobby"

            assertEquals(
                AppResult.Success(ConversationId("conversation-1")),
                repository.startDirectChat(UserId("bob")),
            )

            val rows = repository.observeAll().first()

            assertEquals(1, rows.size)
            assertEquals("Bobby", rows.single().peer.displayName)
            assertEquals(2, remote.startCalls)
        }

    @Test
    fun selfChatIsRejectedBeforeCallingRemote() =
        runBlocking {
            assertTrue(
                repository.startDirectChat(UserId("alice")) is AppResult.Failure,
            )
            assertEquals(0, remote.startCalls)
            assertTrue(repository.observeAll().first().isEmpty())
        }

    @Test
    fun failedFetchPreservesExistingCache() =
        runBlocking {
            assertEquals(
                AppResult.Success(ConversationId("conversation-1")),
                repository.startDirectChat(UserId("bob")),
            )

            val before = repository.observeAll().first()
            remote.failFetch = true

            assertEquals(
                AppResult.Failure(AppError.NoNetwork),
                repository.startDirectChat(UserId("bob")),
            )
            assertEquals(before, repository.observeAll().first())
        }
}
