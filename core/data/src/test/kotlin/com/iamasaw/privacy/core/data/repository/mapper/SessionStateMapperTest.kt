package com.iamasaw.privacy.core.data.repository.mapper

import com.iamasaw.privacy.core.model.SessionState
import com.iamasaw.privacy.core.model.UserId
import io.github.jan.supabase.auth.status.RefreshFailureCause
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import okio.IOException
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.ExperimentalTime

class SessionStateMapperTest {
    @Test
    fun `initializing maps to loading`() {
        assertEquals(
            SessionState.Loading,
            SessionStatus.Initializing.toSessionState(),
        )
    }

    @Test
    fun `signed out maps to signed out`() {
        assertEquals(
            SessionState.SignedOut,
            SessionStatus.NotAuthenticated().toSessionState(),
        )
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `authenticated with user maps to signed it`() {
        val state =
            SessionStatus
                .Authenticated(
                    session =
                        UserSession(
                            accessToken = "access-token",
                            refreshToken = "refresh-token",
                            expiresIn = 3600,
                            tokenType = "bearer",
                            user = UserInfo(id = "user-1", aud = "authenticated"),
                        ),
                ).toSessionState()

        assertEquals(
            SessionState.SignedIn(UserId("user-1")),
            state,
        )
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `authenticated without user maps to signed out`() {
        val state =
            SessionStatus
                .Authenticated(
                    session =
                        UserSession(
                            accessToken = "access-token",
                            refreshToken = "refresh-token",
                            expiresIn = 3600,
                            tokenType = "bearer",
                            user = null,
                        ),
                ).toSessionState()

        assertEquals(SessionState.SignedOut, state)
    }

    @Test
    fun `refresh failure maps to signed out`() {
        val state =
            SessionStatus
                .RefreshFailure(
                    cause = RefreshFailureCause.NetworkError(IOException("offline")),
                ).toSessionState()

        assertEquals(SessionState.SignedOut, state)
    }
}
