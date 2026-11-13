package com.iamasaw.privacy.core.data.repository

import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.data.repository.mapper.runCatchingApp
import com.iamasaw.privacy.core.data.repository.mapper.toUser
import com.iamasaw.privacy.core.model.User
import com.iamasaw.privacy.core.network.ProfileRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultProfileRepository
    @Inject
    constructor(
        private val remote: ProfileRemoteDataSource,
    ) : ProfileRepository {
        override suspend fun createProfile(
            username: String,
            displayName: String?,
        ): AppResult<User> =
            runCatchingApp {
                remote.createProfile(username, displayName).toUser()
            }

        override suspend fun findByUsername(username: String): AppResult<User?> =
            runCatchingApp {
                remote.findByUsername(username)?.toUser()
            }

        override suspend fun fetchOwnProfile(): AppResult<User?> =
            runCatchingApp {
                remote.fetchOwnProfile()?.toUser()
            }
    }
