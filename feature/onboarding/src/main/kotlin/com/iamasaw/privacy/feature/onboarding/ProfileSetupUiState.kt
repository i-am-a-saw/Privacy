package com.iamasaw.privacy.feature.onboarding

import com.iamasaw.privacy.core.common.error.AppError

enum class UsernameAvailability {
    IDLE, INVALID, PENDING, CHECKING, TAKEN, AVAILABLE, FAILED,
}

data class ProfileSetupUiState(
    val username: String = "",
    val displayName: String = "",
    val availability: UsernameAvailability = UsernameAvailability.IDLE,
    val isSubmitting: Boolean = false,
    val lookupError: AppError? = null,
    val submitError: AppError? = null,
    val profileCreated: Boolean = false,
) {
    val canSubmit: Boolean
        get() = availability == UsernameAvailability.AVAILABLE && !isSubmitting && !profileCreated
}
