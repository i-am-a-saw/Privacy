package com.iamasaw.privacy.core.data.repository

import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.data.repository.mapper.runCatchingApp
import com.iamasaw.privacy.core.data.repository.mapper.toSessionState
import com.iamasaw.privacy.core.model.SessionState
import com.iamasaw.privacy.core.network.AuthRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultAuthRepository
    @Inject
    constructor(
        private val remote: AuthRemoteDataSource,
    ) : AuthRepository {
        override val sessionState: Flow<SessionState> =
            remote.sessionStatus.map { it.toSessionState() }.distinctUntilChanged()

        override suspend fun requestCode(phone: String): AppResult<Unit> = runCatchingApp { remote.requestCode(phone) }

        override suspend fun verifyCode(
            phone: String,
            code: String,
        ): AppResult<Unit> = runCatchingApp { remote.verifyCode(phone, code) }

        override suspend fun signOut(): AppResult<Unit> = runCatchingApp { remote.signOut() }
    }
