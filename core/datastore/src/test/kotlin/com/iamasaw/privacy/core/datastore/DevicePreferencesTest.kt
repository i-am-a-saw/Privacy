package com.iamasaw.privacy.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class DevicePreferencesTest {
    @get:Rule
    val directory = TemporaryFolder()

    @Test
    fun freshPreferencesHaveNoPendingCleanup() =
        runTest {
            val preferences = DevicePreferences(createStore(backgroundScope))

            assertFalse(preferences.isSignOutPending())
        }

    @Test
    fun clearingPreferencesKeepsMarkerUntilCompletion() =
        runTest {
            val store = createStore(backgroundScope)
            val usernameKey = stringPreferencesKey("username")
            val preferences = DevicePreferences(store)

            store.edit { it[usernameKey] = "alice" }

            preferences.beginSignOut()
            preferences.clearUserPreferences()

            assertTrue(preferences.isSignOutPending())
            assertNull(store.data.first()[usernameKey])

            preferences.completeSignOut()

            assertFalse(preferences.isSignOutPending())
            assertTrue(
                store.data
                    .first()
                    .asMap()
                    .isEmpty(),
            )
        }

    private fun createStore(scope: CoroutineScope): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = {
                File(directory.root, "test.preferences_pb")
            },
        )
}
