package com.iamasaw.privacy.feature.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.common.time.Clock
import com.iamasaw.privacy.core.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private val PHONE_PATTERN = Regex("^\\+[1-9][0-9]{1,14}$")

@HiltViewModel
class PhoneEntryViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val clock: Clock,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(
        PhoneEntryUiState(
            phone = savedStateHandle.get<String>("phone").orEmpty(),
            codeRequest = savedStateHandle.get<String>("pending_phone")?.let {
                CodeRequest(
                    phone = it,
                    resendAtEpochMillis = savedStateHandle.get<Long>("pending_deadline") ?: 0L
                )
            })
    )

    val uiState = mutableUiState.asStateFlow()

    fun onPhoneChanged(phone: String) {
        if (uiState.value.isSubmitting) return
        savedStateHandle["phone"] = phone
        mutableUiState.value = uiState.value.copy(
            phone = phone,
            invalidPhone = false,
            error = null,
        )
    }

    fun submit() {
        if (uiState.value.isSubmitting) return
        val phone = uiState.value.phone.trim()

        if (!PHONE_PATTERN.matches(phone)) {
            mutableUiState.value = uiState.value.copy(invalidPhone = true)
            return
        }

        mutableUiState.value = uiState.value.copy(
            isSubmitting = true, error = null, invalidPhone = false
        )

        viewModelScope.launch {
            when (val result = repository.requestCode(phone)) {
                is AppResult.Success -> {
                    val deadline = clock.now().plusSeconds(RESEND_DELAY_SECONDS).toEpochMilli()

                    savedStateHandle["pending_phone"] = phone
                    savedStateHandle["pending_deadline"] = deadline
                    mutableUiState.value = uiState.value.copy(
                        isSubmitting = false,
                        codeRequest = CodeRequest(phone = phone, resendAtEpochMillis = deadline)
                    )
                }

                is AppResult.Failure -> {
                    mutableUiState.value = uiState.value.copy(
                        isSubmitting = false,
                        error = result.error
                    )
                }
            }
        }
    }

    fun onCodeOpened() {
        savedStateHandle.remove<String>("pending_phone")
        savedStateHandle.remove<String>("pending_deadline")
        mutableUiState.value = uiState.value.copy(codeRequest = null)
    }
}
