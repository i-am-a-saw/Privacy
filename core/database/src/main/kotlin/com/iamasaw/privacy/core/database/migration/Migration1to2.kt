package com.iamasaw.privacy.core.database.migration

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

object Migration1to2 : Migration(1, 2) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
                CREATE TABLE IF NOT EXISTS conversations (
                    id TEXT NOT NULL PRIMARY KEY,
                    peer_user_id TEXT NOT NULL,
                    peer_username TEXT NOT NULL,
                    peer_display_name TEXT NOT NULL,
                    created_at_micros INTEGER NOT NULL,
                    peer_avatar_path TEXT
                )
            """.trimIndent()
        )
        connection.execSQL("""
            CREATE TABLE IF NOT EXISTS messages (
                id TEXT NOT NULL PRIMARY KEY,
                conversation_id TEXT NOT NULL,
                sender_id TEXT NOT NULL,
                text TEXT NOT NULL,
                local_created_at_micros INTEGER NOT NULL,
                server_created_at_micros INTEGER,
                status TEXT NOT NULL,
                is_unread INTEGER NOT NULL,
                FOREIGN KEY(conversation_id) REFERENCES conversations(id) ON DELETE CASCADE
            )
        """.trimIndent())
        connection.execSQL("""
            CREATE INDEX IF NOT EXISTS index_messages_conversation_id ON messages (conversation_id)
        """.trimIndent())
        connection.execSQL("""
            CREATE TABLE IF NOT EXISTS sync_cursor (
                user_id TEXT NOT NULL PRIMARY KEY,
                created_at_micros INTEGER NOT NULL,
                message_id TEXT NOT NULL
            )
        """.trimIndent())
    }

}
