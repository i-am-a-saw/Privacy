package com.iamasaw.privacy.core.model

data class Message(
    val id: MessageId,
    val status: MessageStatus,
    val content: MessageContent,
    val senderId: UserId,
    val conversationId: ConversationId,
)
