package com.iamasaw.privacy.core.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.iamasaw.privacy.core.database.entity.SyncCursorEntity

@Dao
interface SyncCursorDao {
    @Query("SELECT * FROM sync_cursor WHERE user_id = :userId")
    suspend fun getByUserId(userId: String): SyncCursorEntity?

    @Upsert
    suspend fun upsert(cursor: SyncCursorEntity)
}
