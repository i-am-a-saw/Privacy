package com.iamasaw.privacy.core.common.error

sealed interface AppError {
    data object NoNetwork : AppError

    data object InvalidCode : AppError

    data object RateLimited : AppError

    data object UsernameTaken : AppError

    data class Unknown(
        val cause: Throwable? = null,
    ) : AppError
}
