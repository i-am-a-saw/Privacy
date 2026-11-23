package com.iamasaw.privacy.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first

private val SIGN_OUT_PENDING = booleanPreferencesKey("sign_out_pending")

class DevicePreferences(
    private val store: DataStore<Preferences>
) {
    suspend fun isSignOutPending(): Boolean =
        store.data.first()[SIGN_OUT_PENDING] ?: false

    suspend fun beginSignOut() {
        store.edit { preferences ->
            preferences[SIGN_OUT_PENDING] = true
        }
    }

    suspend fun clearUserPreferences() {
        store.edit { preferences ->
            val pending = preferences[SIGN_OUT_PENDING] == true

            preferences.clear()

            if (pending) {
                preferences[SIGN_OUT_PENDING] = true
            }
        }
    }

    suspend fun completeSignOut() {
        store.edit { preferences ->
            preferences[SIGN_OUT_PENDING] = false
        }
    }
}
