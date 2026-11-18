package com.iamasaw.privacy.feature.contacts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iamasaw.privacy.core.designsystem.component.MessengerButton
import com.iamasaw.privacy.core.model.User

@Composable
fun AddContactRoute(
    initialUsername: String, onSaved: () -> Unit, viewModel: AddContactViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(initialUsername) {
        viewModel.initializeUsername(initialUsername)
    }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            onSaved()
        }
    }

    AddContactScreen(
        state = state,
        onUsernameChanged = viewModel::onUsernameChanged,
        onSearch = viewModel::search,
        onSave = viewModel::save,
    )
}

@Composable
fun AddContactScreen(
    state: AddContactUiState,
    onUsernameChanged: (String) -> Unit,
    onSearch: () -> Unit,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            stringResource(R.string.contacts_add), style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = state.username,
            onValueChange = onUsernameChanged,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.busy && !state.saved,
            singleLine = true,
            label = { Text(stringResource(R.string.contacts_username)) },
            isError = state.error == ContactError.INVALID_USERNAME,
        )

        Text(stringResource(R.string.contacts_username_rules))

        MessengerButton(
            text = stringResource(
                if (state.busy && state.profile == null) {
                    R.string.contacts_searching
                } else {
                    R.string.contacts_search
                }
            ),
            onClick = onSearch,
            enabled = !state.busy && !state.saved,
        )

        state.error?.let {
            Text(
                contactErrorText(it),
                color = MaterialTheme.colorScheme.error,
            )
        }

        state.profile?.let { profile ->
            ContactProfileCard(profile)

            MessengerButton(
                text = stringResource(
                    if (state.busy) {
                        R.string.contacts_saving
                    } else {
                        R.string.contacts_save
                    }
                ),
                onClick = onSave,
                enabled = !state.busy && !state.saved,
            )
        }
    }
}

@Composable
private fun ContactProfileCard(
    profile: User
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = profile.displayName, style = MaterialTheme.typography.titleMedium
            )
            Text("@${profile.username}")
        }
    }
}
