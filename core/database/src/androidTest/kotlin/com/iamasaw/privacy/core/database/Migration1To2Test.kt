package com.iamasaw.privacy.core.database

import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.iamasaw.privacy.core.database.entity.ContactEntity
import com.iamasaw.privacy.core.database.migration.Migration1to2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration1To2Test {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val name = "migration-1-2-test.db"

    @get:Rule
    val helper =
        MigrationTestHelper(
            instrumentation = instrumentation,
            file = context.getDatabasePath(name),
            driver = BundledSQLiteDriver(),
            databaseClass = MessengerDatabase::class,
        )

    @Before
    fun resetTestDatabase() {
        context.deleteDatabase(name)
    }

    @Test
    fun migrationPreservesContactsAndMatchesExportedSchema() =
        runTest {
            helper.createDatabase(1).use { connection ->
                connection.execSQL(
                    "INSERT INTO contacts VALUES ('bob-id', 'bob', 'Bob', NULL)",
                )
            }

            helper.runMigrationsAndValidate(2, listOf(Migration1to2)).use { connection ->
                connection.prepare("SELECT user_id, username, display_name FROM contacts").use {
                    assertTrue(it.step())
                    assertEquals("bob-id", it.getText(0))
                    assertEquals("bob", it.getText(1))
                    assertEquals("Bob", it.getText(2))
                    assertFalse(it.step())
                }
            }
        }

    @Test
    fun productionFactoryMigratesAnExistingVersionOneDatabase() =
        runTest {
            helper.createDatabase(1).use { connection ->
                connection.execSQL(
                    "INSERT INTO contacts VALUES ('bob-id', 'bob', 'Bob', NULL)",
                )
            }

            val database = MessengerDatabaseFactory.create(context, Dispatchers.IO, name)
            try {
                assertEquals(
                    listOf(ContactEntity("bob-id", "bob", "Bob")),
                    database.contactDao().observeAll().first(),
                )
                val conversations = database.conversationDao().observeAll().first()
                assertTrue(conversations.isEmpty())
            } finally {
                database.close()
            }
        }
}
