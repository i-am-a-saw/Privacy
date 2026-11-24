package com.iamasaw.privacy.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(
    val id: String,
    @SerialName("conversation_id") val conversationId: String,
    @SerialName("sender_id") val senderId: String,
    @Serializable(with = ByteaSerializer::class) val payload: ByteArray,
    @SerialName("cipher_suite") val cipherSuite: String,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class MessageInsertDto(
    val id: String,
    @SerialName("conversation_id") val conversationId: String,
    @SerialName("sender_id") val senderId: String,
    @Serializable(with = ByteaSerializer::class) val payload: ByteArray,
    @SerialName("cipher_suite") val cipherSuite: String,
)

data class MessageCursorDto(
    val createdAt: String,
    val id: String,
)
