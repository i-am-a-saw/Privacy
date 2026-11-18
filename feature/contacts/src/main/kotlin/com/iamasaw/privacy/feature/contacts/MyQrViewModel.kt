package com.iamasaw.privacy.feature.contacts

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.data.repository.ProfileRepository
import com.iamasaw.privacy.feature.contacts.qr.QrRenderer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyQrUiState(
    val username: String = "",
    val loading: Boolean = false,
    val bitmap: Bitmap? = null,
    val error: ContactError? = null,
)

@HiltViewModel
class MyQrViewModel
    @Inject
    constructor(
        private val profiles: ProfileRepository,
        private val renderer: QrRenderer,
    ) : ViewModel() {
        private val mutableUiState = MutableStateFlow(MyQrUiState())
        val uiState = mutableUiState.asStateFlow()

        init {
            retry()
        }

        fun retry() {
            if (uiState.value.loading) return
            mutableUiState.value = MyQrUiState(loading = true)

            viewModelScope.launch {
                when (val result = profiles.fetchOwnProfile()) {
                    is AppResult.Success -> {
                        val profile = result.value
                        val bitmap = profile?.let { renderer.render(it.username) }

                        mutableUiState.value =
                            if (profile == null || bitmap == null) {
                                MyQrUiState(error = ContactError.UNKNOWN)
                            } else {
                                MyQrUiState(
                                    username = profile.username,
                                    bitmap = bitmap,
                                )
                            }
                    }

                    is AppResult.Failure -> {
                        mutableUiState.value = MyQrUiState(error = result.error.toContactError())
                    }
                }
            }
        }
    }
