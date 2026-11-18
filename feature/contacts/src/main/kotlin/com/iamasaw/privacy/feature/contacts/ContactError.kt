package com.iamasaw.privacy.feature.contacts

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.iamasaw.privacy.core.common.error.AppError

enum class ContactError {
    INVALID_USERNAME,
    NO_NETWORK,
    RATE_LIMITED,
    NOT_FOUND,
    UNKNOWN,
}

internal fun AppError.toContactError(): ContactError =
    when(this) {
        AppError.NoNetwork -> ContactError.NO_NETWORK
        AppError.RateLimited -> ContactError.RATE_LIMITED
        else -> ContactError.UNKNOWN
    }

@Composable
internal fun contactErrorText(error: ContactError): String =
    stringResource(
        when(error) {
            ContactError.INVALID_USERNAME -> R.string.contacts_username_rules
            ContactError.NOT_FOUND -> R.string.contacts_not_found
            ContactError.RATE_LIMITED -> R.string.contacts_rate_limited
            ContactError.NO_NETWORK -> R.string.contacts_no_network
            ContactError.UNKNOWN -> R.string.contacts_unknown_error
        }
    )
