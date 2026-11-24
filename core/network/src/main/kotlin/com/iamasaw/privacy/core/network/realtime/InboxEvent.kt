package com.iamasaw.privacy.core.network.realtime

sealed interface InboxEvent {
    data object Connected : InboxEvent

    data class MessageCreated(
        val messageId: String,
        val conversationId: String,
    ) : InboxEvent
}
