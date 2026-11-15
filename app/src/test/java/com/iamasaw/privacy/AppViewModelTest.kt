package com.iamasaw.privacy

import com.iamasaw.privacy.core.common.error.AppError
import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.model.SessionState
import com.iamasaw.privacy.core.model.User
import com.iamasaw.privacy.core.model.UserId
import com.iamasaw.privacy.core.testing.rule.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(
        authRepository: FakeAuthRepository,
        profileRepository: FakeProfileRepository,
    ) = AppViewModel(
        authRepository = authRepository,
        profileRepository = profileRepository,
    )

    @Test
    fun `initial state is Loading`() =
        runTest {
            val authRepository = FakeAuthRepository()
            val profileRepository = FakeProfileRepository()

            val viewModel =
                createViewModel(
                    authRepository,
                    profileRepository,
                )

            backgroundScope.launch {
                viewModel.appState.collect()
            }

            advanceUntilIdle()

            assertEquals(
                AppState.Loading,
                viewModel.appState.value,
            )
        }

    @Test
    fun `signed out session produces SignedOut state`() =
        runTest {
            val authRepository = FakeAuthRepository()
            val profileRepository = FakeProfileRepository()

            val viewModel =
                createViewModel(
                    authRepository,
                    profileRepository,
                )

            backgroundScope.launch {
                viewModel.appState.collect()
            }

            authRepository.setSessionState(SessionState.SignedOut)

            advanceUntilIdle()

            assertEquals(
                AppState.SignedOut,
                viewModel.appState.value,
            )

            assertEquals(
                0,
                profileRepository.fetchOwnProfileCalls,
            )
        }

    @Test
    fun `signed in user without profile produces NeedsProfile`() =
        runTest {
            val authRepository = FakeAuthRepository()
            val profileRepository =
                FakeProfileRepository().apply {
                    profileResult = AppResult.Success(null)
                }

            val viewModel =
                createViewModel(
                    authRepository,
                    profileRepository,
                )

            backgroundScope.launch {
                viewModel.appState.collect()
            }

            authRepository.setSessionState(
                SessionState.SignedIn(UserId("test-user")),
            )

            advanceUntilIdle()

            assertEquals(
                AppState.NeedsProfile,
                viewModel.appState.value,
            )

            assertEquals(
                1,
                profileRepository.fetchOwnProfileCalls,
            )
        }

    @Test
    fun `signed in user with profile produces Ready`() =
        runTest {
            val authRepository = FakeAuthRepository()
            val profileRepository =
                FakeProfileRepository().apply {
                    profileResult =
                        AppResult.Success(
                            User(
                                id = UserId("test-user"),
                                username = "testuser",
                                displayName = "Test User",
                                avatarPath = null,
                            ),
                        )
                }

            val viewModel =
                createViewModel(
                    authRepository,
                    profileRepository,
                )

            backgroundScope.launch {
                viewModel.appState.collect()
            }

            authRepository.setSessionState(
                SessionState.SignedIn(UserId("test-user")),
            )

            advanceUntilIdle()

            assertEquals(
                AppState.Ready,
                viewModel.appState.value,
            )
        }

    @Test
    fun `profile loading failure does not produce NeedsProfile`() =
        runTest {
            val authRepository = FakeAuthRepository()
            val profileRepository =
                FakeProfileRepository().apply {
                    profileResult = AppResult.Failure(AppError.NoNetwork)
                }

            val viewModel =
                createViewModel(
                    authRepository,
                    profileRepository,
                )

            backgroundScope.launch {
                viewModel.appState.collect()
            }

            authRepository.setSessionState(
                SessionState.SignedIn(UserId("test-user")),
            )

            advanceUntilIdle()

            assertEquals(
                AppState.Loading,
                viewModel.appState.value,
            )
        }

    @Test
    fun `profile failure releases splash and retry loads profile again`() =
        runTest {
            val authRepository = FakeAuthRepository()
            val profileRepository =
                FakeProfileRepository().apply {
                    profileResult = AppResult.Failure(AppError.NoNetwork)
                }
            val viewModel =
                createViewModel(
                    authRepository,
                    profileRepository,
                )

            authRepository.setSessionState(
                SessionState.SignedIn(UserId("test-user")),
            )
            advanceUntilIdle()

            assertEquals(AppError.NoNetwork, viewModel.startupError.value)
            assertEquals(AppState.Loading, viewModel.appState.value)
            assertFalse(viewModel.shouldKeepSplashScreen())

            profileRepository.profileResult = AppResult.Success(null)
            viewModel.refreshProfile()
            advanceUntilIdle()

            assertEquals(AppState.NeedsProfile, viewModel.appState.value)
            assertNull(viewModel.startupError.value)
            assertEquals(2, profileRepository.fetchOwnProfileCalls)
        }

    @Test
    fun `signing out clears a previous startup failure`() =
        runTest {
            val authRepository = FakeAuthRepository()
            val profileRepository =
                FakeProfileRepository().apply {
                    profileResult = AppResult.Failure(AppError.NoNetwork)
                }
            val viewModel =
                createViewModel(
                    authRepository,
                    profileRepository,
                )

            authRepository.setSessionState(
                SessionState.SignedIn(UserId("test-user")),
            )
            advanceUntilIdle()

            authRepository.setSessionState(SessionState.SignedOut)
            advanceUntilIdle()

            assertEquals(AppState.SignedOut, viewModel.appState.value)
            assertNull(viewModel.startupError.value)
            assertFalse(viewModel.shouldKeepSplashScreen())
        }
}
