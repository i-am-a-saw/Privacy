package com.iamasaw.privacy.core.common.error

sealed interface AppError {
    data object NoNetwork : AppError
    data class Unknown(val cause: Throwable? = null) : AppError
}