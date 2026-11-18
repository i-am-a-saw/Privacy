package com.iamasaw.privacy.core.database

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import com.iamasaw.privacy.core.database.dao.ContactDao
import com.iamasaw.privacy.core.database.entity.ContactEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContactDaoTest {
    private lateinit var context: Context
    private lateinit var database: MessengerDatabase
    private lateinit var dao: ContactDao

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        database =
            Room
                .inMemoryDatabaseBuilder<MessengerDatabase>(context)
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()
        dao = database.contactDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun observeAllEmitsContactsInNameOrder() =
        runTest {
            val bob = contact("bob-id", "bob", "bob")
            val alice = contact("alice-id", "alice", "Alice")

            dao.observeAll().test {
                assertEquals(emptyList<ContactEntity>(), awaitItem())

                dao.upsert(bob)
                assertEquals(listOf(bob), awaitItem())

                dao.upsert(alice)
                assertEquals(listOf(alice, bob), awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun upsertUpdatesExistingContactWithoutDuplicatingIt() =
        runTest {
            val original = contact("alice-id", "alice", "Alice")
            val updated =
                original.copy(
                    username = "alice_new",
                    displayName = "Alice Smith",
                )

            dao.upsert(original)
            dao.upsert(updated)

            assertEquals(listOf(updated), dao.observeAll().first())
        }

    @Test
    fun observeByIdEmitsNullAfterDeletion() =
        runTest {
            val alice = contact("alice-id", "alice", "Alice")

            dao.observeById(alice.userId).test {
                assertNull(awaitItem())

                dao.upsert(alice)
                assertEquals(alice, awaitItem())

                assertEquals(1, dao.deleteById(alice.userId))
                assertNull(awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun deleteAllRemovesEveryContact() =
        runTest {
            dao.upsert(contact("alice-id", "alice", "Alice"))
            dao.upsert(contact("bob-id", "bob", "Bob"))

            assertEquals(2, dao.deleteAll())
            assertEquals(emptyList<ContactEntity>(), dao.observeAll().first())
        }

    @Test
    fun contactsSurviveClosingAndReopeningDatabase() =
        runTest {
            val name = "contact-persistence-test.db"
            val alice = contact("alice-id", "alice", "Alice")
            context.deleteDatabase(name)

            var persistentDatabase =
                MessengerDatabaseFactory.create(
                    context = context,
                    queryDispatcher = Dispatchers.IO,
                    name = name,
                )

            try {
                persistentDatabase.contactDao().upsert(alice)
                persistentDatabase.close()

                persistentDatabase =
                    MessengerDatabaseFactory.create(
                        context = context,
                        queryDispatcher = Dispatchers.IO,
                        name = name,
                    )

                assertEquals(
                    listOf(alice),
                    persistentDatabase.contactDao().observeAll().first(),
                )
            } finally {
                persistentDatabase.close()
                context.deleteDatabase(name)
            }
        }

    private fun contact(
        id: String,
        username: String,
        displayName: String,
    ) = ContactEntity(
        userId = id,
        username = username,
        displayName = displayName,
    )
}
