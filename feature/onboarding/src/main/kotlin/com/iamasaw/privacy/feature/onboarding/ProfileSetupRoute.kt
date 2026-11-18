package com.iamasaw.privacy.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ProfileSetupRoute(
    onProfileCreated: () -> Unit,
    viewModel: ProfileSetupViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.profileCreated) {
        if (state.profileCreated) {
            onProfileCreated()
        }
    }

    ProfileSetupScreen(
        state = state,
        onUsernameChanged = viewModel::onUsernameChanged,
        onDisplayNameChanged = viewModel::onDisplayNameChanged,
        onRetryAvailability = viewModel::onRetryAvailability,
        onSubmit = viewModel::submit,
    )
}
