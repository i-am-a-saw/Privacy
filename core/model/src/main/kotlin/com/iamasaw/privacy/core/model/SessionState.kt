package com.iamasaw.privacy.core.model

sealed interface SessionState {
    data object Loading : SessionState

    data object SignedOut : SessionState

    data class SignedIn(
        val userId: UserId,
    ) : SessionState
}
