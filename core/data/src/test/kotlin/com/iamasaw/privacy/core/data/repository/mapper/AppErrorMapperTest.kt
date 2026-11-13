package com.iamasaw.privacy.core.data.repository.mapper

import com.iamasaw.privacy.core.common.error.AppError
import io.github.jan.supabase.auth.exception.AuthErrorCode
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class AppErrorMapperTest {
    @Test
    fun `network failure mapt to no network`() {
        assertEquals(AppError.NoNetwork, IOException().toAppError())
    }

    @Test
    fun `expired otp maps to invalid code`() {
        assertEquals(
            AppError.InvalidCode,
            mapAuthError(errorCode = AuthErrorCode.OtpExpired, statusCode = 400),
        )
    }

    @Test
    fun `sms rate limit maps to rate limit`() {
        assertEquals(
            AppError.RateLimited,
            mapAuthError(
                errorCode = AuthErrorCode.OverSmsSendRateLimit,
                statusCode = 429,
            ),
        )
    }

    @Test
    fun `unique violation maps to username taken`() {
        assertEquals(
            AppError.UsernameTaken,
            mapPostgrestError(code = "23505", statusCode = 409),
        )
    }

    @Test
    fun `unknown auth error maps to unknown`() {
        val error = mapAuthError(errorCode = null, statusCode = 500)
        assertEquals(true, error is AppError.Unknown)
    }
}
