package com.iamasaw.privacy.core.network

import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.StateFlow

interface AuthRemoteDataSource {
    val sessionStatus: StateFlow<SessionStatus>

    suspend fun requestCode(phone: String)

    suspend fun verifyCode(
        phone: String,
        code: String,
    )

    suspend fun signOut()
}
