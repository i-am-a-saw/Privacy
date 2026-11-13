package com.iamasaw.privacy.core.data.repository

import com.iamasaw.privacy.core.network.ProfileRemoteDataSource
import com.iamasaw.privacy.core.network.dto.ProfileDto

class FakeProfileRemoteDataSource : ProfileRemoteDataSource {
    var createResult: Result<ProfileDto> =
        Result.success(
            ProfileDto(
                id = "user-alice",
                username = "alice",
                displayName = "alice",
                avatarPath = null,
            ),
        )

    var findByUserNameResult: Result<ProfileDto?> = Result.success(null)

    var fetchOwnProfileResult: Result<ProfileDto?> = Result.success(null)

    override suspend fun createProfile(
        username: String,
        displayName: String?,
        avatarPath: String?,
    ): ProfileDto = createResult.getOrThrow()

    override suspend fun findByUsername(username: String): ProfileDto? = findByUserNameResult.getOrThrow()

    override suspend fun fetchOwnProfile(): ProfileDto? = fetchOwnProfileResult.getOrThrow()
}
