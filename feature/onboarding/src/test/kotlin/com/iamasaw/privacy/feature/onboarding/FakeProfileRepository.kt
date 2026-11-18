package com.iamasaw.privacy.feature.onboarding

import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.data.repository.ProfileRepository
import com.iamasaw.privacy.core.model.User
import com.iamasaw.privacy.core.model.UserId

class FakeProfileRepository : ProfileRepository {
    var lookup: suspend (String) -> AppResult<User?> = {
        AppResult.Success(null)
    }

    var createResult: AppResult<User> =
        AppResult.Success(
            User(
                id = UserId("test-user"),
                username = "new_user",
                displayName = "Test User",
                avatarPath = null,
            ),
        )

    val lookups = mutableListOf<String>()
    val creations = mutableListOf<Pair<String, String?>>()

    override suspend fun findByUsername(username: String): AppResult<User?> {
        lookups += username
        return lookup(username)
    }

    override suspend fun createProfile(
        username: String,
        displayName: String?,
    ): AppResult<User> {
        creations += username to displayName
        return createResult
    }

    override suspend fun fetchOwnProfile(): AppResult<User?> = AppResult.Success(null)
}
