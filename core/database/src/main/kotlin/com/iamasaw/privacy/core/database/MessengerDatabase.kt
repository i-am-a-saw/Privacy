package com.iamasaw.privacy.core.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.iamasaw.privacy.core.database.dao.ContactDao
import com.iamasaw.privacy.core.database.dao.ConversationDao
import com.iamasaw.privacy.core.database.dao.MessageDao
import com.iamasaw.privacy.core.database.dao.SyncCursorDao
import com.iamasaw.privacy.core.database.entity.ContactEntity
import com.iamasaw.privacy.core.database.entity.ConversationEntity
import com.iamasaw.privacy.core.database.entity.MessageEntity
import com.iamasaw.privacy.core.database.entity.SyncCursorEntity

@Database(
    entities = [
        ContactEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        SyncCursorEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class MessengerDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao

    abstract fun conversationDao(): ConversationDao

    abstract fun messageDao(): MessageDao

    abstract fun syncCursorDao(): SyncCursorDao
}
