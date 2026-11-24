package com.iamasaw.privacy.core.database

import android.content.Context
import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import com.iamasaw.privacy.core.database.entity.ContactEntity
import com.iamasaw.privacy.core.database.entity.ConversationEntity
import com.iamasaw.privacy.core.database.entity.MessageEntity
import com.iamasaw.privacy.core.database.entity.SyncCursorEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatDatabaseTest {
    private lateinit var context: Context
    private lateinit var database: MessengerDatabase

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        database =
            Room
                .inMemoryDatabaseBuilder<MessengerDatabase>(context)
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun summaryUpdatesPreviewAndUnreadCount() =
        runTest {
            database.conversationDao().upsert(conversation())

            database.conversationDao().observeAll().test {
                val empty = awaitItem().single()
                assertNull(empty.lastMessagePreview)
                assertEquals(0, empty.unreadCount)

                database.messageDao().insert(message("message-1", isUnread = true))
                val first = awaitItem().single()
                assertEquals("message-1", first.lastMessagePreview)
                assertEquals(1, first.unreadCount)

                database.messageDao().insert(message("message-2", isUnread = true))
                val second = awaitItem().single()
                assertEquals("message-2", second.lastMessagePreview)
                assertEquals(2, second.unreadCount)

                assertEquals(2, database.messageDao().markConversationRead("chat-1"))
                assertEquals(0, awaitItem().single().unreadCount)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun pagingReturnsNewestFirstAndOnlyFromRequestedConversation() =
        runTest {
            database.conversationDao().upsert(conversation())
            database.conversationDao().upsert(conversation("chat-2"))
            database.messageDao().insert(message("message-1"))
            database.messageDao().insert(message("message-2"))
            database.messageDao().insert(message("other", conversationId = "chat-2"))

            val result =
                database.messageDao().pagingSource("chat-1").load(
                    PagingSource.LoadParams.Refresh(
                        key = null,
                        loadSize = 20,
                        placeholdersEnabled = false,
                    ),
                )

            assertTrue(result is PagingSource.LoadResult.Page)
            val page = result as PagingSource.LoadResult.Page
            assertEquals(listOf("message-2", "message-1"), page.data.map { it.id })
        }

    @Test
    fun duplicateInsertPreservesExistingMessage() =
        runTest {
            database.conversationDao().upsert(conversation())
            val original = message("message-1", isUnread = true)
            database.messageDao().insert(original)

            assertEquals(-1L, database.messageDao().insert(original.copy(text = "duplicate")))
            assertEquals(original, database.messageDao().getById(original.id))
        }

    @Test
    fun pendingMessageSurvivesClosingAndReopening() =
        runTest {
            val name = "chat-persistence-test.db"
            context.deleteDatabase(name)
            val pending = message("pending").copy(status = "PENDING", serverCreatedAtMicros = null)
            var persistent = MessengerDatabaseFactory.create(context, Dispatchers.IO, name)

            try {
                persistent.conversationDao().upsert(conversation())
                persistent.messageDao().insert(pending)
                persistent.close()
                persistent = MessengerDatabaseFactory.create(context, Dispatchers.IO, name)

                assertEquals(listOf(pending), persistent.messageDao().getPending("chat-1"))
            } finally {
                persistent.close()
                context.deleteDatabase(name)
            }
        }

    @Test
    fun messagesAndCursorCommitOrRollbackTogether() =
        runTest {
            database.conversationDao().upsert(conversation())
            val message = message("message-1")
            val cursor = SyncCursorEntity("alice-id", 1_000_000L, message.id)

            try {
                database.withWriteTransaction {
                    database.messageDao().insert(message)
                    database.syncCursorDao().upsert(cursor)
                    throw IllegalStateException("Simulated failure")
                }
                fail("Transaction should fail")
            } catch (_: IllegalStateException) {
                // The simulated failure must roll back both writes.
            }

            assertNull(database.messageDao().getById(message.id))
            assertNull(database.syncCursorDao().getByUserId(cursor.userId))

            database.withWriteTransaction {
                database.messageDao().insert(message)
                database.syncCursorDao().upsert(cursor)
            }

            assertEquals(message, database.messageDao().getById(message.id))
            assertEquals(cursor, database.syncCursorDao().getByUserId(cursor.userId))
        }

    @Test
    fun clearingUserDataRemovesAllFourTables() =
        runTest {
            database.contactDao().upsert(ContactEntity("bob-id", "bob", "Bob"))
            database.conversationDao().upsert(conversation())
            database.messageDao().insert(message("message-1"))
            database.syncCursorDao().upsert(SyncCursorEntity("alice-id", 1_000_000L, "message-1"))

            database.clearAllTables()

            val contacts = database.contactDao().observeAll().first()
            val conversations = database.conversationDao().observeAll().first()
            assertTrue(contacts.isEmpty())
            assertTrue(conversations.isEmpty())
            assertNull(database.messageDao().getById("message-1"))
            assertNull(database.syncCursorDao().getByUserId("alice-id"))
        }

    private fun conversation(id: String = "chat-1") =
        ConversationEntity(
            id = id,
            peerUserId = "bob-id",
            peerUsername = "bob",
            peerDisplayName = "Bob",
            createdAtMicros = 1_000_000L,
        )

    private fun message(
        id: String,
        conversationId: String = "chat-1",
        isUnread: Boolean = false,
    ) = MessageEntity(
        id = id,
        conversationId = conversationId,
        senderId = "bob-id",
        text = id,
        localCreatedAtMicros = 1_000_000L,
        serverCreatedAtMicros = 1_000_000L,
        status = "SENT",
        isUnread = isUnread,
    )
}
