package com.iamasaw.privacy.core.data.repository.defaults

import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.data.repository.LocalDataRepository
import com.iamasaw.privacy.core.data.repository.mapper.runCatchingApp
import com.iamasaw.privacy.core.database.MessengerDatabase
import com.iamasaw.privacy.core.datastore.DevicePreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultLocalDataRepository
    @Inject
    constructor(
        private val database: MessengerDatabase,
        private val preferences: DevicePreferences,
    ) : LocalDataRepository {
        override suspend fun isSignOutPending(): AppResult<Boolean> =
            runCatchingApp {
                preferences.isSignOutPending()
            }

        override suspend fun beginSignOut(): AppResult<Unit> =
            runCatchingApp {
                preferences.beginSignOut()
            }

        override suspend fun clearUserData(): AppResult<Unit> =
            runCatchingApp {
                database.clearAllTables()
                preferences.clearUserPreferences()
            }

        override suspend fun completeSignOut(): AppResult<Unit> =
            runCatchingApp {
                preferences.completeSignOut()
            }
    }
