package com.iamasaw.privacy.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InboxMessageDto(
    val id: String,
    @SerialName("conversation_id") val conversationId: String,
)
