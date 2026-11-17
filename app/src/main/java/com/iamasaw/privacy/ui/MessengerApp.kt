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
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.iamasaw.privacy.AppState
import com.iamasaw.privacy.R
import com.iamasaw.privacy.core.common.error.AppError
import com.iamasaw.privacy.core.designsystem.component.ErrorState
import com.iamasaw.privacy.core.designsystem.component.LoadingIndicator
import com.iamasaw.privacy.core.navigation.AppNavKey
import com.iamasaw.privacy.feature.auth.CodeEntryRoute
import com.iamasaw.privacy.feature.auth.PhoneEntryRoute
import com.iamasaw.privacy.feature.onboarding.ProfileSetupRoute

@Composable
fun MessengerApp(
    appState: AppState,
    startupError: AppError?,
    onRetry: () -> Unit,
    phoneEntryContent: @Composable (
        onCodeRequested: (String, Long) -> Unit,
    ) -> Unit = { onCodeRequested ->
        PhoneEntryRoute(onCodeRequested = onCodeRequested)
    },
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
                    GateNavHost(
                        startDestination = destination,
                        phoneEntryContent = phoneEntryContent,
                        onProfileCreated = onRetry,
                    )
                }
            }
        }
    }
}

@Composable
private fun GateNavHost(
    startDestination: AppNavKey,
    phoneEntryContent: @Composable ((String, Long) -> Unit) -> Unit,
    onProfileCreated: () -> Unit,
) {
    val backStack = rememberNavBackStack(startDestination)

    NavDisplay(
        backStack = backStack,
        modifier = Modifier.fillMaxSize(),
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
        onBack = {
            if (backStack.size > 1) {
                backStack.removeAt(backStack.lastIndex)
            }
        },
        entryProvider =
            entryProvider {
                entry<AppNavKey.PhoneEntry> {
                    phoneEntryContent { phone, deadline ->
                        if (backStack.lastOrNull() == AppNavKey.PhoneEntry) {
                            backStack.add(AppNavKey.CodeEntry(phone, deadline))
                        }
                    }
                }
                entry<AppNavKey.CodeEntry> { destination ->
                    CodeEntryRoute(
                        phone = destination.phone,
                        resendAtEpochMillis = destination.resendApEpochMillis,
                    )
                }
                entry<AppNavKey.ProfileSetup> {
                    ProfileSetupRoute(onProfileCreated = onProfileCreated)
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
