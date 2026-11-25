package com.iamasaw.privacy.core.data.repository

import com.iamasaw.privacy.core.common.error.AppError

sealed interface SendPendingResult {
    data object Complete : SendPendingResult

    data class Retry(
        val error: AppError,
    ) : SendPendingResult

    data class Failed(
        val error: AppError,
    ) : SendPendingResult
}

@Suppress("MagicNumber")
internal fun isPermanentSendStatus(status: Int): Boolean = status in 400..499 && status !in setOf(401, 408, 425, 429)
