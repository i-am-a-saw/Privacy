package com.iamasaw.privacy.core.data.usecase

import com.iamasaw.privacy.core.common.error.AppError
import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.data.repository.AuthRepository
import com.iamasaw.privacy.core.data.repository.LocalDataRepository
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

enum class SignOutState {
    CHECKING,
    IDLE,
    RUNNING,
    FAILED,
}

@Singleton
class SignOutUseCase
    @Inject
    constructor(
        private val authRepository: AuthRepository,
        private val localData: LocalDataRepository,
    ) {
        private val mutableState = MutableStateFlow(SignOutState.CHECKING)
        val state = mutableState.asStateFlow()

        private val mutex = Mutex()

        suspend fun restoreIfNeeded(): AppResult<Unit> =
            withContext(NonCancellable) {
                mutex.withLock {
                    if (state.value != SignOutState.CHECKING) {
                        AppResult.Success(Unit)
                    } else {
                        when (val pending = localData.isSignOutPending()) {
                            is AppResult.Failure -> {
                                failure()
                            }

                            is AppResult.Success -> {
                                if (pending.value) {
                                    performSignOut()
                                } else {
                                    mutableState.value = SignOutState.IDLE
                                    AppResult.Success(Unit)
                                }
                            }
                        }
                    }
                }
            }

        suspend operator fun invoke(): AppResult<Unit> =
            withContext(NonCancellable) {
                mutex.withLock {
                    performSignOut()
                }
            }

        private suspend fun performSignOut(): AppResult<Unit> {
            mutableState.value = SignOutState.RUNNING

            val steps =
                listOf<suspend () -> AppResult<Unit>>(
                    localData::beginSignOut,
                    localData::clearUserData,
                    authRepository::signOut,
                    localData::completeSignOut,
                )

            for (step in steps) {
                if (step() is AppResult.Failure) {
                    return failure()
                }
            }

            mutableState.value = SignOutState.IDLE
            return AppResult.Success(Unit)
        }

        private fun failure(): AppResult<Unit> {
            mutableState.value = SignOutState.FAILED
            return AppResult.Failure(AppError.LocalDataCleanupFailed)
        }
    }
