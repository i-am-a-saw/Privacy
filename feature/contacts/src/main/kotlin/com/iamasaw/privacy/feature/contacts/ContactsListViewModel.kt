package com.iamasaw.privacy.feature.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamasaw.privacy.core.data.repository.ContactRepository
import com.iamasaw.privacy.core.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class ContactsListUiState(
    val loading: Boolean = true,
    val contacts: List<User> = emptyList(),
    val failed: Boolean = false,
)

@HiltViewModel
class ContactsListViewModel
    @Inject
    constructor(
        private val repository: ContactRepository,
    ) : ViewModel() {
        private val mutableUiState = MutableStateFlow(ContactsListUiState())
        val uiState = mutableUiState.asStateFlow()

        private var observation: Job? = null

        init {
            retry()
        }

        fun retry() {
            observation?.cancel()
            mutableUiState.value = ContactsListUiState()

            observation =
                repository
                    .observeAll()
                    .onEach { contacts ->
                        mutableUiState.value =
                            ContactsListUiState(
                                loading = false,
                                contacts = contacts,
                            )
                    }.catch {
                        mutableUiState.value =
                            ContactsListUiState(
                                loading = false,
                                failed = true,
                            )
                    }.launchIn(viewModelScope)
        }
    }
