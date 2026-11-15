package com.iamasaw.privacy

import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.data.repository.AuthRepository
import com.iamasaw.privacy.core.model.SessionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAuthRepository : AuthRepository {
    private val _sessionState =
        MutableStateFlow<SessionState>(
            SessionState.Loading,
        )

    override val sessionState = _sessionState.asStateFlow()

    fun setSessionState(state: SessionState) {
        _sessionState.value = state
    }

    override suspend fun requestCode(phone: String): AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun verifyCode(
        phone: String,
        code: String,
    ): AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun signOut(): AppResult<Unit> {
        setSessionState(SessionState.SignedOut)
        return AppResult.Success(Unit)
    }
}
