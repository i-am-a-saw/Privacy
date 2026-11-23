package com.iamasaw.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamasaw.privacy.core.common.error.AppError
import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.data.repository.AuthRepository
import com.iamasaw.privacy.core.data.repository.ProfileRepository
import com.iamasaw.privacy.core.data.usecase.SignOutState
import com.iamasaw.privacy.core.data.usecase.SignOutUseCase
import com.iamasaw.privacy.core.model.SessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private data class GateInput(
    val session: SessionState,
    val signOut: SignOutState,
)

@HiltViewModel
class AppViewModel @Inject constructor(
    authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val signOutUseCase: SignOutUseCase,
) : ViewModel() {
    private val mutableAppState =
        MutableStateFlow<AppState>(AppState.Loading)
    private val mutableStartupError =
        MutableStateFlow<AppError?>(null)

    val appState = mutableAppState.asStateFlow()
    val startupError = mutableStartupError.asStateFlow()

    private val refreshRequests = MutableStateFlow(0)
    private var signedOutPrepared = false

    init {
        viewModelScope.launch {
            signOutUseCase.restoreIfNeeded()
        }

        viewModelScope.launch {
            combine(
                authRepository.sessionState,
                refreshRequests,
                signOutUseCase.state,
            ) { session, _, signOut ->
                GateInput(session, signOut)
            }.collect { input ->
                updateGate(input)
            }
        }
    }

    fun refreshProfile() {
        if (signOutUseCase.state.value == SignOutState.FAILED) {
            viewModelScope.launch {
                signOutUseCase()
            }
        } else {
            refreshRequests.update { it + 1 }
        }
    }

    fun shouldKeepSplashScreen(): Boolean =
        appState.value == AppState.Loading &&
                startupError.value == null

    private suspend fun updateGate(input: GateInput) {
        mutableStartupError.value = null
        mutableAppState.value = AppState.Loading

        when (input.signOut) {
            SignOutState.CHECKING,
            SignOutState.RUNNING -> Unit

            SignOutState.IDLE -> {
                when (input.session) {
                    SessionState.Loading -> Unit
                    SessionState.SignedOut -> prepareSignedOut()

                    is SessionState.SignedIn -> {
                        signedOutPrepared = false
                        loadProfile()
                    }
                }
            }

            SignOutState.FAILED -> {
                mutableStartupError.value =
                    AppError.LocalDataCleanupFailed
            }
        }
    }

    private suspend fun prepareSignedOut() {
        val result =
            if (signedOutPrepared) {
                AppResult.Success(Unit)
            } else {
                signedOutPrepared = true
                signOutUseCase()
            }

        when (result) {
            is AppResult.Success -> {
                mutableAppState.value = AppState.SignedOut
            }

            is AppResult.Failure -> {
                mutableStartupError.value =
                    AppError.LocalDataCleanupFailed
            }
        }
    }

    private suspend fun loadProfile() {
        when (val result = profileRepository.fetchOwnProfile()) {
            is AppResult.Failure -> {
                mutableStartupError.value = result.error
            }

            is AppResult.Success -> {
                mutableAppState.value =
                    if (result.value == null) {
                        AppState.NeedsProfile
                    } else {
                        AppState.Ready
                    }
            }
        }
    }
}
