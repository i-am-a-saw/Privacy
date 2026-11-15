package com.iamasaw.privacy

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.iamasaw.privacy.core.designsystem.theme.MessengerTheme
import com.iamasaw.privacy.ui.MessengerApp
import org.junit.Rule
import org.junit.Test

class AuthGateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun signingOutReplacesHomeWithSignIn() {
        val context =
            InstrumentationRegistry.getInstrumentation().targetContext
        val homeTitle = context.getString(R.string.gate_home_title)
        val signInTitle = context.getString(R.string.gate_sign_in_title)
        val state = mutableStateOf<AppState>(AppState.Ready)

        composeRule.setContent {
            MessengerTheme {
                MessengerApp(
                    appState = state.value,
                    startupError = null,
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithText(homeTitle).assertIsDisplayed()

        composeRule.runOnIdle {
            state.value = AppState.SignedOut
        }

        composeRule.onNodeWithText(signInTitle).assertIsDisplayed()
        composeRule.onNodeWithText(homeTitle).assertDoesNotExist()
    }
}
