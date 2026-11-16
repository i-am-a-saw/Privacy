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
fun PhoneEntryScreen(
    state: PhoneEntryUiState, onPhoneChanged: (String) -> Unit, onSubmit: () -> Unit
) {
    val error = if (state.invalidPhone) {
        stringResource(R.string.auth_invalid_phone)
    } else {
        authErrorText(state.error)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            stringResource(R.string.auth_phone_title),
            style = MaterialTheme.typography.headlineMedium
        )
        Text(stringResource(R.string.auth_phone_hint))
        OutlinedTextField(
            value = state.phone,
            onValueChange = onPhoneChanged,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSubmitting,
            label = { Text(stringResource(R.string.auth_phone_label)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
            ),
            singleLine = true,
            isError = error != null,
        )
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        MessengerButton(
            text = stringResource(
                if (state.isSubmitting) {
                    R.string.auth_sending
                } else {
                    R.string.auth_send_code
                },
            ),
            onClick = onSubmit,
            enabled = !state.isSubmitting,
        )
    }
}
