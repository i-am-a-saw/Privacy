package com.iamasaw.privacy.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.iamasaw.privacy.AppState
import com.iamasaw.privacy.R
import com.iamasaw.privacy.core.common.error.AppError
import com.iamasaw.privacy.core.designsystem.component.ErrorState
import com.iamasaw.privacy.core.designsystem.component.LoadingIndicator
import com.iamasaw.privacy.core.navigation.AppNavKey

@Composable
fun MessengerApp(
    appState: AppState,
    startupError: AppError?,
    onRetry: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .safeDrawingPadding(),
        ) {
            val destination =
                when (appState) {
                    AppState.Loading -> null
                    AppState.NeedsProfile -> AppNavKey.ProfileSetup
                    AppState.Ready -> AppNavKey.Home
                    AppState.SignedOut -> AppNavKey.PhoneEntry
                }
            if (destination == null) {
                if (startupError == null) {
                    LoadingIndicator(Modifier.fillMaxSize())
                } else {
                    ErrorState(
                        message =
                            stringResource(
                                if (startupError == AppError.NoNetwork) {
                                    R.string.gate_network_error
                                } else {
                                    R.string.gate_startup_error
                                },
                            ),
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                        onRetry = onRetry,
                    )
                }
            } else {
                key(destination) {
                    GateNavHost(destination)
                }
            }
        }
    }
}

@Composable
private fun GateNavHost(startDestination: AppNavKey) {
    val backStack = rememberNavBackStack(startDestination)

    NavDisplay(
        backStack = backStack,
        modifier = Modifier.fillMaxSize(),
        onBack = {
            if (backStack.size > 1) {
                backStack.removeAt(backStack.lastIndex)
            }
        },
        entryProvider =
            entryProvider {
                entry<AppNavKey.PhoneEntry> {
                    GateDestination(
                        title = stringResource(R.string.gate_sign_in_title),
                        message = stringResource(R.string.gate_sign_in_message),
                    )
                }
                entry<AppNavKey.ProfileSetup> {
                    GateDestination(
                        title = stringResource(R.string.gate_profile_title),
                        message = stringResource(R.string.gate_profile_message),
                    )
                }
                entry<AppNavKey.Home> {
                    GateDestination(
                        title = stringResource(R.string.gate_home_title),
                        message = stringResource(R.string.gate_home_message),
                    )
                }
            },
    )
}

@Composable
private fun GateDestination(
    title: String,
    message: String,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.spacedBy(
                16.dp,
                Alignment.CenterVertically,
            ),
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text(message, style = MaterialTheme.typography.bodyLarge)
    }
}
