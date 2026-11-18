package com.iamasaw.privacy.feature.onboarding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamasaw.privacy.core.common.error.AppError
import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val USERNAME_DEBOUNCE_MILLIS = 400L
private val USERNAME_PATTERN = Regex("^[a-z0-9_]{3,20}$")

private data class UsernameQuery(
    val username: String,
    val revision: Long = 0L,
)

private fun internalAvailability(username: String): UsernameAvailability =
    when {
        username.isEmpty() -> UsernameAvailability.IDLE
        !USERNAME_PATTERN.matches(username) -> UsernameAvailability.INVALID
        else -> UsernameAvailability.PENDING
    }

@OptIn(FlowPreview::class)
@HiltViewModel
class ProfileSetupViewModel
    @Inject
    constructor(
        private val repository: ProfileRepository,
        private val savedState: SavedStateHandle,
    ) : ViewModel() {
        private val restoredUsername = savedState.get<String>("username").orEmpty()

        private val mutableUiState =
            MutableStateFlow(
                ProfileSetupUiState(
                    username = restoredUsername,
                    displayName = savedState.get<String>("display_name").orEmpty(),
                    availability = internalAvailability(restoredUsername),
                ),
            )

        val uiState = mutableUiState.asStateFlow()

        private val queries =
            MutableStateFlow(
                UsernameQuery(restoredUsername),
            )

        init {
            viewModelScope.launch {
                queries.debounce(USERNAME_DEBOUNCE_MILLIS).collectLatest { query ->
                    checkAvailability(query)
                }
            }
        }

        fun onUsernameChanged(username: String) {
            val state = uiState.value
            if (state.isSubmitting || state.profileCreated) return
            if (state.username == username) return

            savedState["username"] = username
            mutableUiState.value =
                uiState.value.copy(
                    username = username,
                    availability = internalAvailability(username),
                    lookupError = null,
                    submitError = null,
                )

            queries.value =
                UsernameQuery(
                    username = username,
                    revision = queries.value.revision + 1,
                )
        }

        fun onDisplayNameChanged(displayName: String) {
            if (uiState.value.isSubmitting || uiState.value.profileCreated) return

            savedState["display_name"] = displayName
            mutableUiState.value =
                uiState.value.copy(
                    displayName = displayName,
                    submitError = null,
                )
        }

        fun onRetryAvailability() {
            val state = uiState.value
            if (state.isSubmitting || state.profileCreated) return
            if (!USERNAME_PATTERN.matches(state.username)) return

            mutableUiState.value =
                state.copy(
                    availability = UsernameAvailability.PENDING,
                    lookupError = null,
                )

            queries.value =
                queries.value.copy(
                    revision = queries.value.revision + 1,
                )
        }

        fun submit() {
            val state = uiState.value
            if (!state.canSubmit) return

            mutableUiState.value =
                state.copy(
                    isSubmitting = true,
                    submitError = null,
                )

            viewModelScope.launch {
                when (
                    val result =
                        repository.createProfile(
                            username = state.username,
                            displayName = state.displayName.trim().ifEmpty { null },
                        )
                ) {
                    is AppResult.Success -> {
                        mutableUiState.value =
                            state.copy(
                                isSubmitting = false,
                                profileCreated = true,
                            )
                    }

                    is AppResult.Failure -> {
                        when (result.error) {
                            is AppError.UsernameTaken -> {
                                mutableUiState.value =
                                    state.copy(
                                        isSubmitting = false,
                                        availability = UsernameAvailability.TAKEN,
                                        submitError = null,
                                    )
                            }

                            else -> {
                                mutableUiState.value =
                                    state.copy(
                                        isSubmitting = false,
                                        submitError = result.error,
                                    )
                            }
                        }
                    }
                }
            }
        }

        private suspend fun checkAvailability(query: UsernameQuery) {
            if (!isCurrentQuery(query) || !USERNAME_PATTERN.matches(query.username)) return

            mutableUiState.value =
                uiState.value.copy(
                    availability = UsernameAvailability.CHECKING,
                    lookupError = null,
                )

            val result = repository.findByUsername(query.username)
            if (!isCurrentQuery(query)) return

            mutableUiState.value =
                when (result) {
                    is AppResult.Success -> {
                        if (result.value == null) {
                            uiState.value.copy(
                                availability = UsernameAvailability.AVAILABLE,
                                lookupError = null,
                            )
                        } else {
                            uiState.value.copy(
                                availability = UsernameAvailability.TAKEN,
                                lookupError = null,
                            )
                        }
                    }

                    is AppResult.Failure -> {
                        uiState.value.copy(
                            availability = UsernameAvailability.FAILED,
                            lookupError = result.error,
                        )
                    }
                }
        }

        private fun isCurrentQuery(query: UsernameQuery): Boolean =
            query == queries.value && !uiState.value.isSubmitting && !uiState.value.profileCreated
    }
