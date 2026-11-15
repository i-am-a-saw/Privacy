package com.iamasaw.privacy.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface AppNavKey : NavKey {
    @Serializable
    data object PhoneEntry : AppNavKey

    @Serializable
    data object ProfileSetup : AppNavKey

    @Serializable
    data object Home : AppNavKey
}
