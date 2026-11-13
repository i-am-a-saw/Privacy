package com.iamasaw.privacy.core.data.repository.mapper

import com.iamasaw.privacy.core.model.SessionState
import com.iamasaw.privacy.core.model.SessionState.SignedIn
import com.iamasaw.privacy.core.model.UserId
import io.github.jan.supabase.auth.status.SessionStatus

fun SessionStatus.toSessionState(): SessionState =
    when (this) {
        SessionStatus.Initializing -> {
            SessionState.Loading
        }

        is SessionStatus.Authenticated -> {
            session.user?.id?.let { SignedIn(UserId(it)) }
                ?: SessionState.SignedOut
        }

        is SessionStatus.NotAuthenticated -> {
            SessionState.SignedOut
        }

        is SessionStatus.RefreshFailure -> {
            SessionState.SignedOut
        }
    }
