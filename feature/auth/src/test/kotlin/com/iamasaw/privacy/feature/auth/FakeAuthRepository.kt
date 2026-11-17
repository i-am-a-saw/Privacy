package com.iamasaw.privacy.feature.auth

import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.data.repository.AuthRepository
import com.iamasaw.privacy.core.model.SessionState
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAuthRepository : AuthRepository {
    override val sessionState =
        MutableStateFlow<SessionState>(SessionState.SignedOut)

    var requestResult: AppResult<Unit> = AppResult.Success(Unit)
    var verifyResult: AppResult<Unit> = AppResult.Success(Unit)

    val requestedPhones = mutableListOf<String>()
    val verifications = mutableListOf<Pair<String, String>>()

    override suspend fun requestCode(phone: String): AppResult<Unit> {
        requestedPhones += phone
        return requestResult
    }

    override suspend fun verifyCode(
        phone: String,
        code: String,
    ): AppResult<Unit> {
        verifications += phone to code
        return verifyResult
    }

    override suspend fun signOut(): AppResult<Unit> = AppResult.Success(Unit)
}
