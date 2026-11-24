package com.iamasaw.privacy.core.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "sync_cursor")
data class SyncCursorEntity(
    @PrimaryKey @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "created_at_micros") val createdAtMicros: Long,
    @ColumnInfo(name = "message_id") val messageId: String,
)
