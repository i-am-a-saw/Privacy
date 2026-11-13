package com.iamasaw.privacy.core.data.repository

import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.model.User

interface ProfileRepository {
    suspend fun createProfile(
        username: String,
        displayName: String? = null,
    ): AppResult<User>

    suspend fun findByUsername(username: String): AppResult<User?>

    suspend fun fetchOwnProfile(): AppResult<User?>
}
