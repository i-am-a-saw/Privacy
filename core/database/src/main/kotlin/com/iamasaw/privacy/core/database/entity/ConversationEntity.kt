package com.iamasaw.privacy.core.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "peer_user_id") val peerUserId: String,
    @ColumnInfo(name = "peer_username") val peerUsername: String,
    @ColumnInfo(name = "peer_display_name") val peerDisplayName: String,
    @ColumnInfo(name = "created_at_micros") val createdAtMicros: Long,
    @ColumnInfo(name = "peer_avatar_path") val peerAvatarPath: String? = null,
)
