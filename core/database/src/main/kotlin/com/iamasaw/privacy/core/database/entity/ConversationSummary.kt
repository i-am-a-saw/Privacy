package com.iamasaw.privacy.core.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Embedded

data class ConversationSummary(
    @Embedded val conversation: ConversationEntity,
    @ColumnInfo(name = "last_message_preview") val lastMessagePreview: String?,
    @ColumnInfo(name = "last_message_at_micros") val lastMessageAtMicros: Long?,
    @ColumnInfo(name = "unread_count") val unreadCount: Int,
)
