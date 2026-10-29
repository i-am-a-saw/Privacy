package com.iamasaw.privacy.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors =
    lightColorScheme(
        primary = Teal40,
        secondary = Slate40,
        error = Red40,
    )

private val DarkColors =
    darkColorScheme(
        primary = Teal80,
        secondary = Slate80,
        error = Red80,
    )

@Composable
fun MessengerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MessengerTypography,
        content = content,
    )
}
