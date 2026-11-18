package com.iamasaw.privacy.core.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.iamasaw.privacy.core.database.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query(
        """
            SELECT * FROM contacts
            ORDER BY display_name COLLATE NOCASE ASC, user_id ASC
        """,
    )
    fun observeAll(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE user_id = :userId")
    fun observeById(userId: String): Flow<ContactEntity?>

    @Upsert
    suspend fun upsert(contact: ContactEntity)

    @Query("DELETE FROM contacts WHERE user_id = :userId")
    suspend fun deleteById(userId: String): Int

    @Query("DELETE FROM contacts")
    suspend fun deleteAll(): Int
}
