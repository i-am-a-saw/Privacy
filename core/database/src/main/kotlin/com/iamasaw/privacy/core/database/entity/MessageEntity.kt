package com.iamasaw.privacy.core.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversation_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["conversation_id"])],
)
data class MessageEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "conversation_id") val conversationId: String,
    @ColumnInfo(name = "sender_id") val senderId: String,
    val text: String,
    @ColumnInfo(name = "local_created_at_micros") val localCreatedAtMicros: Long,
    @ColumnInfo(name = "server_created_at_micros") val serverCreatedAtMicros: Long? = null,
    val status: String = "PENDING",
    @ColumnInfo(name = "is_unread") val isUnread: Boolean = false,
)
