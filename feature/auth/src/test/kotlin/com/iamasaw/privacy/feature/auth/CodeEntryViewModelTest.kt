package com.iamasaw.privacy.feature.auth

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.iamasaw.privacy.core.common.error.AppError
import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.testing.rule.MainDispatcherRule
import com.iamasaw.privacy.core.testing.time.TestClock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class CodeEntryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `successful verification marks code as verified`() =
        runTest {
            val repository = FakeAuthRepository()
            val clock = TestClock()
            val viewModel =
                CodeEntryViewModel(
                    repository,
                    clock,
                    SavedStateHandle(),
                )
            viewModel.initialize(
                "+16505550101",
                clock.now().plusSeconds(60).toEpochMilli(),
            )

            viewModel.uiState.test {
                awaitItem()
                viewModel.onCodeChanged("123456")
                viewModel.verifyCode()
                runCurrent()

                assertTrue(expectMostRecentItem().verified)
                assertEquals(
                    listOf("+16505550101" to "123456"),
                    repository.verifications,
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `incorrect code exposes error and permits another attempt`() =
        runTest {
            val repository =
                FakeAuthRepository().apply {
                    verifyResult = AppResult.Failure(AppError.InvalidCode)
                }
            val clock = TestClock()
            val viewModel =
                CodeEntryViewModel(
                    repository,
                    clock,
                    SavedStateHandle(),
                )
            viewModel.initialize(
                "+16505550101",
                clock.now().plusSeconds(60).toEpochMilli(),
            )

            viewModel.uiState.test {
                awaitItem()
                viewModel.onCodeChanged("000000")
                viewModel.verifyCode()
                runCurrent()

                val state = expectMostRecentItem()
                assertEquals(AppError.InvalidCode, state.error)
                assertFalse(state.isSubmitting)
                assertFalse(state.verified)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `resend waits for clock deadline and restarts countdown`() =
        runTest {
            val repository = FakeAuthRepository()
            val clock = TestClock()
            val viewModel =
                CodeEntryViewModel(
                    repository,
                    clock,
                    SavedStateHandle(),
                )
            viewModel.initialize(
                "+16505550101",
                clock.now().plusSeconds(60).toEpochMilli(),
            )

            viewModel.uiState.test {
                assertEquals(60, awaitItem().secondsRemaining)

                viewModel.resendCode()
                assertTrue(repository.requestedPhones.isEmpty())

                clock.advanceBy(60.seconds)
                viewModel.refreshCountdown()
                assertEquals(0, awaitItem().secondsRemaining)

                viewModel.resendCode()
                runCurrent()

                assertEquals(60, expectMostRecentItem().secondsRemaining)
                assertEquals(listOf("+16505550101"), repository.requestedPhones)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
