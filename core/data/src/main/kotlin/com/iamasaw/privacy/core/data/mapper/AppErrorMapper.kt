package com.iamasaw.privacy.core.data.repository.mapper

import com.iamasaw.privacy.core.common.error.AppError
import com.iamasaw.privacy.core.common.result.AppResult
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import kotlinx.coroutines.CancellationException
import java.io.IOException

private val INVALID_CODE_CODES =
    setOf(
        AuthErrorCode.OtpExpired,
        AuthErrorCode.InvalidCredentials,
        AuthErrorCode.OtpDisabled,
    )

private val RATE_LIMIT_CODES =
    setOf(
        AuthErrorCode.OverRequestRateLimit,
        AuthErrorCode.OverSmsSendRateLimit,
        AuthErrorCode.OverEmailSendRateLimit,
    )

private const val UNIQUE_VIOLATION = "23505"
private const val TOO_MANY_REQUESTS_STATUS_CODE = 429

fun Throwable.toAppError(): AppError =
    when (this) {
        is AuthRestException -> {
            mapAuthError(errorCode, statusCode)
        }

        is PostgrestRestException -> {
            mapPostgrestError(code, statusCode)
        }

        is RestException -> {
            if (statusCode == TOO_MANY_REQUESTS_STATUS_CODE) {
                AppError.RateLimited
            } else {
                AppError.Unknown(this)
            }
        }

        is IOException -> {
            AppError.NoNetwork
        }

        else -> {
            AppError.Unknown(this)
        }
    }

internal fun mapAuthError(
    errorCode: AuthErrorCode?,
    statusCode: Int,
): AppError =
    when {
        statusCode == TOO_MANY_REQUESTS_STATUS_CODE -> AppError.RateLimited
        errorCode != null && errorCode in INVALID_CODE_CODES -> AppError.InvalidCode
        errorCode != null && errorCode in RATE_LIMIT_CODES -> AppError.RateLimited
        else -> AppError.Unknown()
    }

internal fun mapPostgrestError(
    code: String?,
    statusCode: Int,
): AppError =
    when {
        code == UNIQUE_VIOLATION -> AppError.UsernameTaken
        statusCode == TOO_MANY_REQUESTS_STATUS_CODE -> AppError.RateLimited
        else -> AppError.Unknown()
    }

suspend inline fun <T> runCatchingApp(block: suspend () -> T): AppResult<T> =
    try {
        AppResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        AppResult.Failure(e.toAppError())
    }
