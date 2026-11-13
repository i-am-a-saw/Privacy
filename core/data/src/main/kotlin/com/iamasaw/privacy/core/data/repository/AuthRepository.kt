package com.iamasaw.privacy.core.data.repository

import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.model.SessionState
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val sessionState: Flow<SessionState>

    suspend fun requestCode(phone: String): AppResult<Unit>

    suspend fun verifyCode(
        phone: String,
        code: String,
    ): AppResult<Unit>

    suspend fun signOut(): AppResult<Unit>
}
