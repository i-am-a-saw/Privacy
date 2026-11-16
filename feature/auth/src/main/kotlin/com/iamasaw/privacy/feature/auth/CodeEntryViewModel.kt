package com.iamasaw.privacy.feature.auth

import androidx.compose.ui.platform.isDebugInspectorInfoEnabled
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamasaw.privacy.core.common.error.AppError
import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.common.time.Clock
import com.iamasaw.privacy.core.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val OTP_LENGTH = 6
private const val MILLIS_PER_SECOND = 1_000L

@HiltViewModel
class CodeEntryViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val clock: Clock,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(CodeEntryUiState())
    val uiState = mutableUiState.asStateFlow()

    fun initialize(phone: String, resendAtEpochMillis: Long) {
        if (uiState.value.phone.isNotEmpty()) return

        if (savedStateHandle.get<Long>("resend_deadline") == null) {
            savedStateHandle["resend_deadline"] = resendAtEpochMillis
        }

        mutableUiState.value = uiState.value.copy(phone = phone)
        refreshCountdown()
    }

    fun onCodeChanged(code: String) {
        if (uiState.value.isSubmitting || uiState.value.verified) return
        mutableUiState.value = uiState.value.copy(
            code = code.filter { it in '0'..'9' }.take(OTP_LENGTH),
            error = null,
        )
    }

    fun refreshCountdown() {
        val deadline = savedStateHandle.get<Long>("resend_deadline") ?: 0L
        val remainingMillis = (deadline - clock.now().toEpochMilli()).coerceAtLeast(0L)
        val seconds = ((remainingMillis + MILLIS_PER_SECOND - 1) / MILLIS_PER_SECOND).coerceAtMost(
            RESEND_DELAY_SECONDS
        ).toInt()

        mutableUiState.value = uiState.value.copy(secondsRemaining = seconds)
    }

    fun verifyCode() {
        val state = uiState.value
        if (state.verified || state.isSubmitting || state.phone.isEmpty()) return

        if (state.code.length != OTP_LENGTH) {
            mutableUiState.value = uiState.value.copy(
                error = AppError.InvalidCode
            )
            return
        }

        mutableUiState.value = uiState.value.copy(
            isSubmitting = true,
            error = null,
        )

        viewModelScope.launch {
            when(val result = repository.verifyCode(state.phone, state.code)) {
                is AppResult.Success -> {
                    mutableUiState.value = uiState.value.copy(
                        verified = true,
                        isSubmitting = false,
                        error = null
                    )
                }

                is AppResult.Failure -> {
                    mutableUiState.value = uiState.value.copy(
                        isSubmitting = false,
                        error = result.error,
                    )
                }
            }
        }
    }

    fun resendCode() {
        refreshCountdown()
        val state = uiState.value

        if (
            state.isSubmitting || state.verified ||
            state.secondsRemaining > 0 || state.phone.isEmpty()
        ) return

        mutableUiState.value = uiState.value.copy(
            isSubmitting = true, error = null,
        )

        viewModelScope.launch {
            when(val result = repository.requestCode(state.phone)){
                is AppResult.Failure -> {
                    mutableUiState.value = uiState.value.copy(
                        isSubmitting = false,
                        error = result.error,
                    )
                }
                is AppResult.Success -> {
                    savedStateHandle["resend_deadline"] = clock.now()
                        .plusSeconds(RESEND_DELAY_SECONDS)
                        .toEpochMilli()
                    mutableUiState.value = uiState.value.copy(
                        code = "",
                        isSubmitting = false,
                    )
                }
            }
        }
    }
}
