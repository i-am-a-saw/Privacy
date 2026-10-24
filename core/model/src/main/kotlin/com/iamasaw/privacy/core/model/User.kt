package com.iamasaw.privacy.core.model

data class User (
    val id: UserId,

    val username: String,
    val displayName: String,
    val avatarPath: String?,
)