package com.iamasaw.privacy.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.iamasaw.privacy.core.designsystem.component.MessengerButton

@Composable
fun CodeEntryScreen(
    state: CodeEntryUiState,
    onCodeChanged: (String) -> Unit,
    onVerify: () -> Unit,
    onResend: () -> Unit,
) {
    val error = authErrorText(state.error)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            stringResource(R.string.auth_code_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(stringResource(R.string.auth_code_sent_to, state.phone))
        OutlinedTextField(
            value = state.code,
            onValueChange = onCodeChanged,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSubmitting && !state.verified,
            label = { Text(stringResource(R.string.auth_code_label)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword
            ),
            singleLine = true,
            isError = error != null,
        )
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        MessengerButton(
            text = stringResource(
                when {
                    state.verified -> R.string.auth_verified
                    state.isSubmitting -> R.string.auth_sending
                    else -> R.string.auth_verify
                },
            ),
            onClick = onVerify,
            enabled = !state.isSubmitting && !state.verified,
        )
        MessengerButton(
            text = if (state.secondsRemaining > 0) {
                stringResource(
                    R.string.auth_resend_countdown,
                    state.secondsRemaining,
                )
            } else {
                stringResource(R.string.auth_resend)
            },
            onClick = onResend,
            enabled = state.secondsRemaining == 0 &&
                    !state.isSubmitting && !state.verified,
        )
    }
}
