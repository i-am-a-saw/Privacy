package com.iamasaw.privacy.core.model

import java.time.Instant

data class Message(
    val id: MessageId,
    val status: MessageStatus,
    val content: MessageContent,
    val senderId: UserId,
    val conversationId: ConversationId,
    val localCreatedAt: Instant,
    val serverCreatedAt: Instant?,
)
