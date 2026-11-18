package com.iamasaw.privacy.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface AppNavKey : NavKey {
    @Serializable
    data object Home : AppNavKey

    @Serializable
    data object PhoneEntry : AppNavKey

    @Serializable
    data class CodeEntry(
        val phone: String,
        val resendApEpochMillis: Long,
    ) : AppNavKey

    @Serializable
    data object ProfileSetup : AppNavKey

    @Serializable
    data class AddContact(
        val username: String = "",
    ) : AppNavKey

    @Serializable
    data object MyQr : AppNavKey

    @Serializable
    data object ScanQr : AppNavKey
}
