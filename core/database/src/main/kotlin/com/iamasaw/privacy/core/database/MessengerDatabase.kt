package com.iamasaw.privacy.core.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.iamasaw.privacy.core.database.dao.ContactDao
import com.iamasaw.privacy.core.database.entity.ContactEntity

@Database(
    entities = [ContactEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class MessengerDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
}
