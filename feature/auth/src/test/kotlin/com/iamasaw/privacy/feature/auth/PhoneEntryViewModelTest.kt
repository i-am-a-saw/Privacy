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

@OptIn(ExperimentalCoroutinesApi::class)
class PhoneEntryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `invalid phone does not request a code`() =
        runTest {
            val repository = FakeAuthRepository()
            val viewModel =
                PhoneEntryViewModel(
                    repository,
                    TestClock(),
                    SavedStateHandle(),
                )

            viewModel.uiState.test {
                awaitItem()
                viewModel.onPhoneChanged("123")
                viewModel.submit()
                runCurrent()

                assertTrue(expectMostRecentItem().invalidPhone)
                assertTrue(repository.requestedPhones.isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `successful request exposes phone and resend deadline`() =
        runTest {
            val repository = FakeAuthRepository()
            val clock = TestClock()
            val viewModel =
                PhoneEntryViewModel(
                    repository,
                    clock,
                    SavedStateHandle(),
                )

            viewModel.uiState.test {
                awaitItem()
                viewModel.onPhoneChanged("+16505550101")
                viewModel.submit()
                runCurrent()

                val state = expectMostRecentItem()
                assertFalse(state.isSubmitting)
                assertEquals(
                    CodeRequest(
                        "+16505550101",
                        clock.now().plusSeconds(60).toEpochMilli(),
                    ),
                    state.codeRequest,
                )
                assertEquals(listOf("+16505550101"), repository.requestedPhones)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `network failure preserves phone and exposes error`() =
        runTest {
            val repository =
                FakeAuthRepository().apply {
                    requestResult = AppResult.Failure(AppError.NoNetwork)
                }
            val viewModel =
                PhoneEntryViewModel(
                    repository,
                    TestClock(),
                    SavedStateHandle(),
                )

            viewModel.uiState.test {
                awaitItem()
                viewModel.onPhoneChanged("+16505550101")
                viewModel.submit()
                runCurrent()

                val state = expectMostRecentItem()
                assertEquals("+16505550101", state.phone)
                assertEquals(AppError.NoNetwork, state.error)
                assertFalse(state.isSubmitting)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
