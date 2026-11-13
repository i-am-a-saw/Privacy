package com.iamasaw.privacy.core.data.repository

import com.iamasaw.privacy.core.network.AuthRemoteDataSource
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeAuthRemoteDataSource : AuthRemoteDataSource {
    val status = MutableStateFlow<SessionStatus>(SessionStatus.Initializing)
    override val sessionStatus: StateFlow<SessionStatus> = status

    var requestCodeResult: Result<Unit> = Result.success(Unit)
    var verifyCodeResult: Result<Unit> = Result.success(Unit)
    var signOutResult: Result<Unit> = Result.success(Unit)

    override suspend fun requestCode(phone: String) = requestCodeResult.getOrThrow()

    override suspend fun verifyCode(
        phone: String,
        code: String,
    ) = verifyCodeResult.getOrThrow()

    override suspend fun signOut() = signOutResult.getOrThrow()
}
