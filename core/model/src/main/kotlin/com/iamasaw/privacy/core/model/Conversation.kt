package com.iamasaw.privacy.core.model

import java.time.Instant

data class Conversation(
    val id: ConversationId,
    val peer: User,
    val lastMessagePreview: String? = null,
    val lastMessageAt: Instant? = null,
    val unreadCount: Int = 0,
)
