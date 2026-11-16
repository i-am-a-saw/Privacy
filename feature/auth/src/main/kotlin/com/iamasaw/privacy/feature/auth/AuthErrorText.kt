package com.iamasaw.privacy.feature.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.iamasaw.privacy.core.common.error.AppError

@Composable
internal fun authErrorText(error: AppError?): String? =
    when (error) {
        AppError.InvalidCode -> stringResource(R.string.auth_invalid_code)
        AppError.NoNetwork -> stringResource(R.string.auth_no_network)
        AppError.RateLimited -> stringResource(R.string.auth_rate_limited)
        null -> null
        else -> stringResource(R.string.auth_unknown_error)
    }
