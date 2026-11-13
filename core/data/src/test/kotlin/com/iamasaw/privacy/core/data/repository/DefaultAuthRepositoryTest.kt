package com.iamasaw.privacy.core.data.repository

import com.iamasaw.privacy.core.common.error.AppError
import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.model.SessionState
import com.iamasaw.privacy.core.model.UserId
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okio.IOException
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.ExperimentalTime

class DefaultAuthRepositoryTest {
    @OptIn(ExperimentalTime::class)
    @Test
    fun `session status maps to sesstion state`() =
        runTest {
            val fake = FakeAuthRemoteDataSource()
            val repository = DefaultAuthRepository(fake)

            fake.status.value =
                SessionStatus.Authenticated(
                    session =
                        UserSession(
                            accessToken = "access-token",
                            refreshToken = "refresh-token",
                            expiresIn = 3600,
                            tokenType = "bearer",
                            user = UserInfo(id = "user-1", aud = "authenticated"),
                        ),
                )

            assertEquals(
                SessionState.SignedIn(UserId("user-1")),
                repository.sessionState.first(),
            )
        }

    @Test
    fun `verify code network failure is returned as no network`() =
        runTest {
            val fake =
                FakeAuthRemoteDataSource().apply {
                    verifyCodeResult = Result.failure(IOException("offline"))
                }
            val repository = DefaultAuthRepository(fake)

            assertEquals(
                AppResult.Failure(AppError.NoNetwork),
                repository.verifyCode(phone = "+16505550101", code = "123456"),
            )
        }
}
