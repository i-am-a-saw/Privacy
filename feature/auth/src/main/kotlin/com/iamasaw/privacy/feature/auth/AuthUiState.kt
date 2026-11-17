package com.iamasaw.privacy.feature.auth

import com.iamasaw.privacy.core.common.error.AppError

internal const val RESEND_DELAY_SECONDS = 60L

data class CodeRequest(
    val phone: String,
    val resendAtEpochMillis: Long,
)

data class PhoneEntryUiState(
    val phone: String = "",
    val isSubmitting: Boolean = false,
    val invalidPhone: Boolean = false,
    val error: AppError? = null,
    val codeRequest: CodeRequest? = null,
)

data class CodeEntryUiState(
    val phone: String = "",
    val code: String = "",
    val secondsRemaining: Int = 0,
    val isSubmitting: Boolean = false,
    val verified: Boolean = false,
    val error: AppError? = null,
)
