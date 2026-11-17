package com.iamasaw.privacy.feature.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private const val DELAY_MILLIS = 1_000L

@Composable
fun PhoneEntryRoute(
    onCodeRequested: (String, Long) -> Unit,
    viewModel: PhoneEntryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.codeRequest) {
        state.codeRequest?.let { request ->
            onCodeRequested(request.phone, request.resendAtEpochMillis)
            viewModel.onCodeOpened()
        }
    }

    PhoneEntryScreen(
        state = state,
        onPhoneChanged = viewModel::onPhoneChanged,
        onSubmit = viewModel::submit,
    )
}

@Composable
fun CodeEntryRoute(
    phone: String,
    resendAtEpochMillis: Long,
    viewModel: CodeEntryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(viewModel, phone, resendAtEpochMillis, lifecycleOwner) {
        viewModel.initialize(phone, resendAtEpochMillis)

        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (isActive) {
                viewModel.refreshCountdown()
                delay(DELAY_MILLIS)
            }
        }
    }

    CodeEntryScreen(
        state = state,
        onCodeChanged = viewModel::onCodeChanged,
        onVerify = viewModel::verifyCode,
        onResend = viewModel::resendCode,
    )
}
