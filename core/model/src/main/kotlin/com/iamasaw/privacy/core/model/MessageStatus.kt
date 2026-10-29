package com.iamasaw.privacy.core.model

enum class MessageStatus {
    PENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED,
    ;

    fun canTransitionTo(next: MessageStatus): Boolean = TODO()
}