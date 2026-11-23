package com.iamasaw.privacy.core.data.repository

import com.iamasaw.privacy.core.common.result.AppResult

interface LocalDataRepository {
    suspend fun isSignOutPending(): AppResult<Boolean>

    suspend fun beginSignOut(): AppResult<Unit>

    suspend fun clearUserData(): AppResult<Unit>

    suspend fun completeSignOut(): AppResult<Unit>
}
