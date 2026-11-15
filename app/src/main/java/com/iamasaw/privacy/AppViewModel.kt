package com.iamasaw.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamasaw.privacy.core.common.error.AppError
import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.data.repository.AuthRepository
import com.iamasaw.privacy.core.data.repository.ProfileRepository
import com.iamasaw.privacy.core.model.SessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel
    @Inject
    constructor(
        authRepository: AuthRepository,
        private val profileRepository: ProfileRepository,
    ) : ViewModel() {
        private val mutableAppState =
            MutableStateFlow<AppState>(AppState.Loading)

        val appState = mutableAppState.asStateFlow()

        private val mutableStartupError =
            MutableStateFlow<AppError?>(null)

        val startupError = mutableStartupError.asStateFlow()

        private val refreshRequests = MutableStateFlow(0)

        init {
            viewModelScope.launch {
                combine(
                    authRepository.sessionState,
                    refreshRequests,
                ) { session, _ ->
                    session
                }.collectLatest { session ->
                    mutableStartupError.value = null
                    mutableAppState.value = AppState.Loading

                    when (session) {
                        SessionState.Loading -> {
                            Unit
                        }

                        is SessionState.SignedIn -> {
                            loadProfile()
                        }

                        SessionState.SignedOut -> {
                            mutableAppState.value = AppState.SignedOut
                        }
                    }
                }
            }
        }

        fun refreshProfile() {
            refreshRequests.update { it + 1 }
        }

        fun shouldKeepSplashScreen(): Boolean =
            appState.value == AppState.Loading &&
                startupError.value == null

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
