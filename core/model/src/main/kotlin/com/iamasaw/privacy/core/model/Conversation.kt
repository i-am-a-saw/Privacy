package com.iamasaw.privacy.core.model

data class Conversation(
    val id: ConversationId,

    val users: List<User>,
)