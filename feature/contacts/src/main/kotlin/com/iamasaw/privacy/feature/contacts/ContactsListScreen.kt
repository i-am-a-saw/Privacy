package com.iamasaw.privacy.feature.contacts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iamasaw.privacy.core.designsystem.component.MessengerButton

@Composable
fun ContactsListRoute(
    onAddContact: () -> Unit,
    onMyQr: () -> Unit,
    onScanQr: () -> Unit,
    viewModel: ContactsListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            ContactsListScreen(
                state = state,
                onAddContact = onAddContact,
                onMyQr = onMyQr,
                onScanQr = onScanQr,
                onRetry = viewModel::retry,
            )
        }

        SignOutButton(onConfirm = viewModel::signOut)
    }
}

@Composable
fun ContactsListScreen(
    state: ContactsListUiState,
    onAddContact: () -> Unit,
    onMyQr: () -> Unit,
    onScanQr: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            stringResource(R.string.contacts_title),
            style = MaterialTheme.typography.headlineMedium,
        )

        MessengerButton(
            text = stringResource(R.string.contacts_add),
            onClick = onAddContact,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MessengerButton(
                text = stringResource(R.string.contacts_my_qr),
                onClick = onMyQr,
            )
            MessengerButton(
                text = stringResource(R.string.contacts_scan_qr),
                onClick = onScanQr,
            )
        }

        when {
            state.loading -> {
                Text(stringResource(R.string.contacts_loading))
            }

            state.failed -> {
                Text(
                    stringResource(R.string.contacts_unknown_error),
                    color = MaterialTheme.colorScheme.error,
                )
                MessengerButton(
                    text = stringResource(R.string.contacts_retry),
                    onClick = onRetry,
                )
            }

            state.contacts.isEmpty() -> {
                Text(stringResource(R.string.contacts_empty))
            }

            else -> {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(
                        items = state.contacts,
                        key = { it.id.value },
                    ) { contact ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                contact.displayName,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text("@${contact.username}")
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
