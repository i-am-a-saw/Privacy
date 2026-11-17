package com.iamasaw.privacy.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iamasaw.privacy.core.common.error.AppError
import com.iamasaw.privacy.core.designsystem.component.MessengerButton

@Composable
fun ProfileSetupScreen(
    state: ProfileSetupUiState,
    onUsernameChanged: (String) -> Unit,
    onDisplayNameChanged: (String) -> Unit,
    onRetryAvailability: () -> Unit,
    onSubmit: () -> Unit,
) {
    val editable = !state.isSubmitting && !state.profileCreated
    val usernameHasError =
        state.availability == UsernameAvailability.FAILED || state.availability == UsernameAvailability.TAKEN ||
            state.availability == UsernameAvailability.INVALID

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(
            stringResource(R.string.profile_setup_title),
            style = MaterialTheme.typography.headlineMedium,
        )

        OutlinedTextField(
            value = state.username,
            onValueChange = onUsernameChanged,
            modifier = Modifier.fillMaxWidth(),
            enabled = editable,
            label = { Text(stringResource(R.string.profile_username_label)) },
            singleLine = true,
            isError = usernameHasError,
        )

        Text(
            text = availabilityText(state.availability),
            color =
                if (usernameHasError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
        )

        if (state.availability == UsernameAvailability.FAILED) {
            Text(
                text = profileErrorText(state.lookupError),
                color = MaterialTheme.colorScheme.error,
            )
            MessengerButton(
                text = stringResource(R.string.profile_username_failed),
                enabled = editable,
                onClick = onRetryAvailability,
            )
        }

        OutlinedTextField(
            value = state.displayName,
            onValueChange = onDisplayNameChanged,
            label = {
                Text(stringResource(R.string.profile_display_name_label))
            },
            singleLine = true,
            enabled = editable,
            modifier = Modifier.fillMaxWidth(),
        )

        state.submitError?.let {
            Text(
                profileErrorText(it),
                color = MaterialTheme.colorScheme.error,
            )
        }

        MessengerButton(
            text =
                stringResource(
                    when {
                        state.profileCreated -> R.string.profile_created
                        state.isSubmitting -> R.string.profile_creating
                        else -> R.string.profile_create
                    },
                ),
            onClick = onSubmit,
            enabled = state.canSubmit,
        )
    }
}

@Composable
private fun availabilityText(availability: UsernameAvailability): String =
    stringResource(
        when (availability) {
            UsernameAvailability.IDLE, UsernameAvailability.INVALID -> R.string.profile_username_rules
            UsernameAvailability.PENDING -> R.string.profile_username_pending
            UsernameAvailability.CHECKING -> R.string.profile_username_checking
            UsernameAvailability.TAKEN -> R.string.profile_username_taken
            UsernameAvailability.AVAILABLE -> R.string.profile_username_available
            UsernameAvailability.FAILED -> R.string.profile_username_failed
        },
    )

@Composable
private fun profileErrorText(error: AppError?): String =
    stringResource(
        when (error) {
            is AppError.NoNetwork -> R.string.profile_no_network
            is AppError.RateLimited -> R.string.profile_rate_limited
            else -> R.string.profile_unknown_error
        },
    )
