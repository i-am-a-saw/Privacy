package com.iamasaw.privacy.feature.contacts

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iamasaw.privacy.core.designsystem.component.MessengerButton

@Composable
internal fun SignOutButton(onConfirm: () -> Unit) {
    var confirming by rememberSaveable { mutableStateOf(false) }

    MessengerButton(
        text = stringResource(R.string.contacts_sign_out),
        onClick = { confirming = true },
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
    )

    if (confirming) {
        AlertDialog(
            onDismissRequest = { confirming = false },
            title = {
                Text(stringResource(R.string.contacts_sign_out_title))
            },
            text = {
                Text(stringResource(R.string.contacts_sign_out_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirming = false
                        onConfirm()
                    },
                ) {
                    Text(stringResource(R.string.contacts_sign_out))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirming = false }) {
                    Text(stringResource(R.string.contacts_cancel))
                }
            },
        )
    }
}
