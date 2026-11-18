package com.iamasaw.privacy.core.database

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.CoroutineDispatcher

private const val DATABASE_NAME = "messenger.db"

object MessengerDatabaseFactory {
    fun create(
        context: Context,
        queryDispatcher: CoroutineDispatcher,
        name: String = DATABASE_NAME
    ): MessengerDatabase =
        Room.databaseBuilder<MessengerDatabase>(
            context = context.applicationContext,
            name = name,
        )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(queryDispatcher)
            .build()
}
