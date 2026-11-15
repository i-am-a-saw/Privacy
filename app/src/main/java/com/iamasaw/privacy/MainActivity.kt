package com.iamasaw.privacy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iamasaw.privacy.core.designsystem.theme.MessengerTheme
import com.iamasaw.privacy.ui.MessengerApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        val viewModel = appViewModel

        splashScreen.setKeepOnScreenCondition {
            viewModel.shouldKeepSplashScreen()
        }

        enableEdgeToEdge()

        setContent {
            val appState by viewModel.appState.collectAsStateWithLifecycle()
            val startUpError by viewModel.startupError.collectAsStateWithLifecycle()

            MessengerTheme {
                MessengerApp(
                    appState = appState,
                    startupError = startUpError,
                    onRetry = viewModel::refreshProfile,
                )
            }
        }
    }
}
