package com.iamasaw.privacy.core.data.usecase

import com.iamasaw.privacy.core.common.error.AppError
import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.data.repository.AuthRepository
import com.iamasaw.privacy.core.data.repository.LocalDataRepository
import com.iamasaw.privacy.core.model.SessionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SignOutUseCaseTest {
    @Test
    fun successRunsCleanupInOrder() =
        runTest {
            val fixture = fixture()

            assertEquals(AppResult.Success(Unit), fixture.useCase())

            assertEquals(
                listOf("begin", "wipe", "auth", "finish"),
                fixture.events,
            )
            assertFalse(fixture.local.pending)
            assertEquals(SignOutState.IDLE, fixture.useCase.state.value)
        }

    @Test
    fun wipeFailureStopsBeforeSessionCleanup() =
        runTest {
            val fixture = fixture()
            fixture.local.clearResult =
                AppResult.Failure(AppError.Unknown())

            assertEquals(
                AppResult.Failure(AppError.LocalDataCleanupFailed),
                fixture.useCase(),
            )

            assertEquals(listOf("begin", "wipe"), fixture.events)
            assertTrue(fixture.local.pending)
            assertEquals(SignOutState.FAILED, fixture.useCase.state.value)
        }

    @Test
    fun sessionCleanupFailureKeepsPendingMarker() =
        runTest {
            val fixture = fixture()
            fixture.auth.result =
                AppResult.Failure(AppError.Unknown())

            fixture.useCase()

            assertEquals(
                listOf("begin", "wipe", "auth"),
                fixture.events,
            )
            assertTrue(fixture.local.pending)
            assertEquals(SignOutState.FAILED, fixture.useCase.state.value)
        }

    @Test
    fun startupResumesPendingCleanup() =
        runTest {
            val fixture = fixture()
            fixture.local.pending = true

            fixture.useCase.restoreIfNeeded()

            assertEquals(
                listOf("read", "begin", "wipe", "auth", "finish"),
                fixture.events,
            )
            assertFalse(fixture.local.pending)
            assertEquals(SignOutState.IDLE, fixture.useCase.state.value)
        }

    @Test
    fun cleanStartupDoesNotSignOutAnExistingSession() =
        runTest {
            val fixture = fixture()

            fixture.useCase.restoreIfNeeded()

            assertEquals(listOf("read"), fixture.events)
            assertEquals(SignOutState.IDLE, fixture.useCase.state.value)
        }

    @Test
    fun failedCleanupCanBeRetried() =
        runTest {
            val fixture = fixture()
            fixture.local.clearResult =
                AppResult.Failure(AppError.Unknown())

            fixture.useCase()
            assertTrue(fixture.local.pending)

            fixture.local.clearResult = AppResult.Success(Unit)

            assertEquals(AppResult.Success(Unit), fixture.useCase())
            assertFalse(fixture.local.pending)
            assertEquals(SignOutState.IDLE, fixture.useCase.state.value)
        }

    private fun fixture(): Fixture {
        val events = mutableListOf<String>()
        val local = FakeLocalDataRepository(events)
        val auth = FakeSignOutAuthRepository(events)

        return Fixture(
            local = local,
            auth = auth,
            useCase = SignOutUseCase(auth, local),
            events = events,
        )
    }
}

private data class Fixture(
    val local: FakeLocalDataRepository,
    val auth: FakeSignOutAuthRepository,
    val useCase: SignOutUseCase,
    val events: MutableList<String>,
)

private class FakeLocalDataRepository(
    private val events: MutableList<String>,
) : LocalDataRepository {
    var pending = false
    var clearResult: AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun isSignOutPending(): AppResult<Boolean> {
        events += "read"
        return AppResult.Success(pending)
    }

    override suspend fun beginSignOut(): AppResult<Unit> {
        events += "begin"
        pending = true
        return AppResult.Success(Unit)
    }

    override suspend fun clearUserData(): AppResult<Unit> {
        events += "wipe"
        return clearResult
    }

    override suspend fun completeSignOut(): AppResult<Unit> {
        events += "finish"
        pending = false
        return AppResult.Success(Unit)
    }
}

private class FakeSignOutAuthRepository(
    private val events: MutableList<String>,
) : AuthRepository {
    var result: AppResult<Unit> = AppResult.Success(Unit)

    override val sessionState: Flow<SessionState> =
        flowOf(SessionState.SignedOut)

    override suspend fun requestCode(phone: String): AppResult<Unit> = error("Not used by these tests")

    override suspend fun verifyCode(
        phone: String,
        code: String,
    ): AppResult<Unit> = error("Not used by these tests")

    override suspend fun signOut(): AppResult<Unit> {
        events += "auth"
        return result
    }
}
