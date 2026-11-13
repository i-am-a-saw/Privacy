package com.iamasaw.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.data.repository.AuthRepository
import com.iamasaw.privacy.core.data.repository.ProfileRepository
import com.iamasaw.privacy.core.model.SessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AppViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
        private val profileRepository: ProfileRepository,
    ) : ViewModel() {
        val appState: StateFlow<AppState> =
            authRepository.sessionState
                .flatMapLatest { sessionState ->

                    when (sessionState) {
                        SessionState.Loading -> {
                            flowOf(AppState.Loading)
                        }

                        SessionState.SignedOut -> {
                            flowOf(AppState.SignedOut)
                        }

                        is SessionState.SignedIn -> {
                            profileRepository.fetchOwnProfile().let { result ->
                                flowOf(
                                    when (result) {
                                        is AppResult.Success -> {
                                            if (result.value == null) {
                                                AppState.NeedsProfile
                                            } else {
                                                AppState.Ready
                                            }
                                        }

                                        is AppResult.Failure -> {
                                            AppState.Loading
                                        }
                                    },
                                )
                            }
                        }
                    }
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = AppState.Loading,
                )
    }
