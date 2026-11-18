package com.iamasaw.privacy.feature.contacts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.common.usernameRule.UsernameRule
import com.iamasaw.privacy.core.data.repository.ContactRepository
import com.iamasaw.privacy.core.data.repository.ProfileRepository
import com.iamasaw.privacy.core.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddContactUiState(
    val username: String = "",
    val profile: User? = null,
    val busy: Boolean = false,
    val error: ContactError? = null,
    val saved: Boolean = false,
)

@HiltViewModel
class AddContactViewModel
    @Inject
    constructor(
        private val profiles: ProfileRepository,
        private val contacts: ContactRepository,
        private val savedState: SavedStateHandle,
    ) : ViewModel() {
        private val mutableUiState = MutableStateFlow(AddContactUiState())
        val uiState = mutableUiState.asStateFlow()

        private var initialized = false

        fun initializeUsername(initialUsername: String) {
            if (initialized) return
            initialized = true

            val username = savedState.get<String>("username") ?: initialUsername
            savedState["username"] = username
            mutableUiState.value = AddContactUiState(username = username)

            if (username.isNotEmpty()) {
                search()
            }
        }

        fun onUsernameChanged(username: String) {
            if (uiState.value.busy || uiState.value.saved) return

            savedState["username"] = username
            mutableUiState.value = AddContactUiState(username = username)
        }

        fun search() {
            val state = uiState.value
            if (state.busy || state.saved) return

            if (!UsernameRule.isValid(state.username)) {
                mutableUiState.value =
                    state.copy(
                        error = ContactError.INVALID_USERNAME,
                        profile = null,
                    )
                return
            }

            mutableUiState.value =
                state.copy(
                    busy = true,
                    profile = null,
                    error = null,
                )

            viewModelScope.launch {
                mutableUiState.value =
                    when (val result = profiles.findByUsername(state.username)) {
                        is AppResult.Success -> {
                            state.copy(
                                profile = result.value,
                                error =
                                    if (result.value == null) {
                                        ContactError.NOT_FOUND
                                    } else {
                                        null
                                    },
                            )
                        }

                        is AppResult.Failure -> {
                            state.copy(
                                error = result.error.toContactError(),
                                profile = null,
                            )
                        }
                    }
            }
        }

        fun save() {
            val state = uiState.value
            val profile = state.profile ?: return
            if (state.busy || state.saved) return

            mutableUiState.value = state.copy(busy = true, error = null)

            viewModelScope.launch {
                mutableUiState.value =
                    when (val result = contacts.saveContact(profile)) {
                        is AppResult.Failure -> {
                            state.copy(
                                error = result.error.toContactError(),
                            )
                        }

                        is AppResult.Success -> {
                            state.copy(
                                saved = true,
                                error = null,
                            )
                        }
                    }
            }
        }
    }
