package com.iamasaw.privacy

import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.data.repository.ProfileRepository
import com.iamasaw.privacy.core.model.User

class FakeProfileRepository : ProfileRepository {
    var profileResult: AppResult<User?> = AppResult.Success(null)

    var fetchOwnProfileCalls = 0

    override suspend fun fetchOwnProfile(): AppResult<User?> {
        fetchOwnProfileCalls++
        return profileResult
    }

    override suspend fun createProfile(
        username: String,
        displayName: String?,
    ): AppResult<User> {
        error("Not implemented in this test")
    }

    override suspend fun findByUsername(username: String): AppResult<User?> {
        error("Not implemented in this test")
    }
}
