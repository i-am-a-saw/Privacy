package com.iamasaw.privacy.core.model

enum class MessageStatus {
    PENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED,
    ;

    fun canTransitionTo(next: MessageStatus): Boolean =
        when (this) {
            PENDING -> next == SENT || next == FAILED
            SENT -> next == READ || next == DELIVERED
            DELIVERED -> next == READ
            READ -> false
            FAILED -> next == PENDING
        }
}
