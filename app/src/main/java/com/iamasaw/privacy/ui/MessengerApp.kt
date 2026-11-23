package com.iamasaw.privacy.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
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
import com.iamasaw.privacy.feature.contacts.AddContactRoute
import com.iamasaw.privacy.feature.contacts.ContactsListRoute
import com.iamasaw.privacy.feature.contacts.MyQrRoute
import com.iamasaw.privacy.feature.contacts.ScanQrScreen
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
    homeContent: @Composable (onNavigate: (AppNavKey) -> Unit) -> Unit = { navigate ->
        ContactsListRoute(
            onAddContact = { navigate(AppNavKey.AddContact()) },
            onMyQr = { navigate(AppNavKey.MyQr) },
            onScanQr = { navigate(AppNavKey.ScanQr) },
        )
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
                                when (startupError) {
                                    AppError.LocalDataCleanupFailed -> R.string.gate_cleanup_error
                                    AppError.NoNetwork -> R.string.gate_network_error
                                    else -> R.string.gate_startup_error
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
                        homeContent = homeContent,
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
    homeContent: @Composable (onNavigate: (AppNavKey) -> Unit) -> Unit,
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
                    homeContent { destination ->
                        if (backStack.lastOrNull() == AppNavKey.Home) {
                            backStack.add(destination)
                        }
                    }
                }
                entry<AppNavKey.AddContact> { destination ->
                    AddContactRoute(
                        initialUsername = destination.username,
                        onSaved = {
                            if (backStack.lastOrNull() == destination) {
                                backStack.removeAt(backStack.lastIndex)
                            }
                        },
                    )
                }

                entry<AppNavKey.MyQr> {
                    MyQrRoute()
                }

                entry<AppNavKey.ScanQr> {
                    ScanQrScreen(
                        onUsername = { username ->
                            if (backStack.lastOrNull() == AppNavKey.ScanQr) {
                                backStack.removeAt(backStack.lastIndex)
                                backStack.add(AppNavKey.AddContact(username))
                            }
                        },
                    )
                }
            },
    )
}
