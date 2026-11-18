package com.iamasaw.privacy.feature.onboarding

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.iamasaw.privacy.core.common.error.AppError
import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.model.User
import com.iamasaw.privacy.core.model.UserId
import com.iamasaw.privacy.core.testing.rule.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileSetupViewModelTest {
    @get:Rule
    val mainDispatcherRule =
        MainDispatcherRule(StandardTestDispatcher())

    @Test
    fun `invalid username does not call repository`() =
        runTest {
            val repository = FakeProfileRepository()
            val viewModel =
                ProfileSetupViewModel(
                    repository,
                    SavedStateHandle(),
                )

            viewModel.onUsernameChanged("Alice!")
            finishCheck()
            viewModel.submit()
            runCurrent()

            assertEquals(
                UsernameAvailability.INVALID,
                viewModel.uiState.value.availability,
            )
            assertTrue(repository.lookups.isEmpty())
            assertTrue(repository.creations.isEmpty())
        }

    @Test
    fun `typing checks only latest username after debounce`() =
        runTest {
            val repository = FakeProfileRepository()
            val viewModel =
                ProfileSetupViewModel(
                    repository,
                    SavedStateHandle(),
                )

            viewModel.uiState.test {
                awaitItem()
                viewModel.onUsernameChanged("alice")
                runCurrent()
                advanceTimeBy(200)
                viewModel.onUsernameChanged("alice_1")
                runCurrent()

                advanceTimeBy(399)
                runCurrent()
                assertTrue(repository.lookups.isEmpty())

                advanceTimeBy(1)
                runCurrent()

                assertEquals(listOf("alice_1"), repository.lookups)
                assertEquals(
                    UsernameAvailability.AVAILABLE,
                    expectMostRecentItem().availability,
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `existing username is taken`() =
        runTest {
            val repository =
                FakeProfileRepository().apply {
                    lookup = { AppResult.Success(existingUser()) }
                }
            val viewModel =
                ProfileSetupViewModel(
                    repository,
                    SavedStateHandle(),
                )

            viewModel.onUsernameChanged("alice")
            finishCheck()

            assertEquals(
                UsernameAvailability.TAKEN,
                viewModel.uiState.value.availability,
            )
            assertFalse(viewModel.uiState.value.canSubmit)
        }

    @Test
    fun `failed lookup can retry the same username`() =
        runTest {
            val repository =
                FakeProfileRepository().apply {
                    lookup = { AppResult.Failure(AppError.NoNetwork) }
                }
            val viewModel =
                ProfileSetupViewModel(
                    repository,
                    SavedStateHandle(),
                )

            viewModel.onUsernameChanged("new_user")
            finishCheck()

            assertEquals(
                UsernameAvailability.FAILED,
                viewModel.uiState.value.availability,
            )
            assertEquals(
                AppError.NoNetwork,
                viewModel.uiState.value.lookupError,
            )

            repository.lookup = { AppResult.Success(null) }
            viewModel.onRetryAvailability()
            finishCheck()

            assertEquals(
                UsernameAvailability.AVAILABLE,
                viewModel.uiState.value.availability,
            )
            assertEquals(listOf("new_user", "new_user"), repository.lookups)
        }

    @Test
    fun `username claimed before submission is shown as taken`() =
        runTest {
            val repository =
                FakeProfileRepository().apply {
                    createResult = AppResult.Failure(AppError.UsernameTaken)
                }
            val viewModel =
                ProfileSetupViewModel(
                    repository,
                    SavedStateHandle(),
                )

            viewModel.onUsernameChanged("new_user")
            finishCheck()
            assertTrue(viewModel.uiState.value.canSubmit)

            viewModel.submit()
            runCurrent()

            assertEquals(
                UsernameAvailability.TAKEN,
                viewModel.uiState.value.availability,
            )
            assertFalse(viewModel.uiState.value.profileCreated)
            assertFalse(viewModel.uiState.value.canSubmit)
        }

    @Test
    fun `successful submission trims display name and completes`() =
        runTest {
            val repository = FakeProfileRepository()
            val viewModel =
                ProfileSetupViewModel(
                    repository,
                    SavedStateHandle(),
                )

            viewModel.onUsernameChanged("new_user")
            viewModel.onDisplayNameChanged("  Alice  ")
            finishCheck()

            viewModel.submit()
            runCurrent()

            assertEquals(
                listOf("new_user" to "Alice"),
                repository.creations,
            )
            assertTrue(viewModel.uiState.value.profileCreated)
            assertFalse(viewModel.uiState.value.isSubmitting)
        }

    @Test
    fun `old response cannot override a newly typed username`() =
        runTest {
            val oldResponse = CompletableDeferred<AppResult<User?>>()
            val repository =
                FakeProfileRepository().apply {
                    lookup = { username ->
                        if (username == "first_name") {
                            oldResponse.await()
                        } else {
                            AppResult.Success(existingUser())
                        }
                    }
                }
            val viewModel =
                ProfileSetupViewModel(
                    repository,
                    SavedStateHandle(),
                )

            viewModel.onUsernameChanged("first_name")
            finishCheck()

            viewModel.onUsernameChanged("second_name")
            oldResponse.complete(AppResult.Success(null))
            runCurrent()

            assertEquals(
                UsernameAvailability.PENDING,
                viewModel.uiState.value.availability,
            )

            finishCheck()

            assertEquals("second_name", viewModel.uiState.value.username)
            assertEquals(
                UsernameAvailability.TAKEN,
                viewModel.uiState.value.availability,
            )
        }

    private fun TestScope.finishCheck() {
        runCurrent()
        advanceTimeBy(400)
        runCurrent()
    }

    private fun existingUser() =
        User(
            id = UserId("existing-user"),
            username = "alice",
            displayName = "Alice",
            avatarPath = null,
        )
}
