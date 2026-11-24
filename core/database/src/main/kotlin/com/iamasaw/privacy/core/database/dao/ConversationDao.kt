package com.iamasaw.privacy.core.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.iamasaw.privacy.core.database.entity.ConversationEntity
import com.iamasaw.privacy.core.database.entity.ConversationSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Upsert
    suspend fun upsert(conversation: ConversationEntity)

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getById(id: String): ConversationEntity?

    @Query("""
        SELECT c.*,
            latest.text AS last_message_preview,
            COALESCE(latest.server_created_at_micros, latest.local_created_at_micros)
                AS last_message_at_micros,
            (SELECT COUNT(*) FROM messages AS unread
             WHERE unread.conversation_id = c.id AND unread.is_unread = 1) AS unread_count
        FROM conversations AS c
        LEFT JOIN messages AS latest ON latest.id = (
            SELECT m.id FROM messages AS m
            WHERE m.conversation_id = c.id
            ORDER BY COALESCE(m.server_created_at_micros, m.local_created_at_micros) DESC,
                m.id DESC
            LIMIT 1
        )
        ORDER BY COALESCE(
            latest.server_created_at_micros, latest.local_created_at_micros, c.created_at_micros
        ) DESC, c.id DESC
    """)
    fun observeAll(): Flow<List<ConversationSummary>>
}
