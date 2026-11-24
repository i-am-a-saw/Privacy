package com.iamasaw.privacy.core.database.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Upsert
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import com.iamasaw.privacy.core.database.entity.MessageEntity

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(message: MessageEntity): Long

    @Upsert
    suspend fun upsert(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getById(id: String): MessageEntity?

    @Query(
        """
        SELECT * FROM messages WHERE conversation_id = :conversationId
        ORDER BY COALESCE(server_created_at_micros, local_created_at_micros) DESC, id DESC
    """,
    )
    fun pagingSource(conversationId: String): PagingSource<Int, MessageEntity>

    @Query(
        """
        SELECT * FROM messages
        WHERE conversation_id = :conversationId AND status = 'PENDING'
        ORDER BY local_created_at_micros ASC, id ASC
    """,
    )
    suspend fun getPending(conversationId: String): List<MessageEntity>

    @Query(
        """
        UPDATE messages SET is_unread = 0
        WHERE conversation_id = :conversationId AND is_unread = 1
    """,
    )
    suspend fun markConversationRead(conversationId: String): Int
}
