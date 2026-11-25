package com.iamasaw.privacy.core.data.repository

import com.iamasaw.privacy.core.data.usecase.SignOutState
import com.iamasaw.privacy.core.data.usecase.SignOutUseCase
import com.iamasaw.privacy.core.model.SessionState
import com.iamasaw.privacy.core.model.UserId
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.flow.first
import javax.inject.Inject

internal class CurrentChatUser
    @Inject
    constructor(
        private val auth: AuthRepository,
        private val signOut: SignOutUseCase,
    ) {
        suspend fun requireId(): UserId {
            requireReady()

            val state =
                auth.sessionState.first {
                    it != SessionState.Loading
                }

            requireReady()

            return (state as? SessionState.SignedIn)?.userId
                ?: throw CancellationException("No authenticated chat user")
        }

        private fun requireReady() {
            if (signOut.state.value != SignOutState.IDLE) {
                throw CancellationException(
                    "Chat is unavailable during session cleanup",
                )
            }
        }

        suspend fun check(expected: UserId) {
            if (requireId() != expected) {
                throw CancellationException(
                    "The authenticated chat user changed",
                )
            }
        }
    }
