package com.iamasaw.privacy.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileInsertDto(
    val id: String,
    val username: String,
    @SerialName("display_name") val displayName: String?,
    @SerialName("avatar_path") val avatarPath: String? = null,
)
