package com.iamasaw.privacy.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConversationDto(
    val id: String,
    val type: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("created_by") val createdy: String,
    val members: List<ConversationMemberDto>,
)

@Serializable
data class ConversationMemberDto(
    @SerialName("user_id") val userId: String,
    val profile: ProfileDto?,
)
