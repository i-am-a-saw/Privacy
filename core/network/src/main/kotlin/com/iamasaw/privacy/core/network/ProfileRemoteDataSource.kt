package com.iamasaw.privacy.core.network

import com.iamasaw.privacy.core.network.dto.ProfileDto

interface ProfileRemoteDataSource {
    suspend fun createProfile(
        username: String,
        displayName: String?,
        avatarPath: String? = null,
    ): ProfileDto

    suspend fun findByUsername(username: String): ProfileDto?

    suspend fun fetchOwnProfile(): ProfileDto?
}
