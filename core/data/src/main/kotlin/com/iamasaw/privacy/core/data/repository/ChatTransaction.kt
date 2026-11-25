package com.iamasaw.privacy.core.data.repository

import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import com.iamasaw.privacy.core.database.MessengerDatabase

internal suspend fun <T> MessengerDatabase.chatTransaction(block: suspend () -> T): T =
    useWriterConnection { writer ->
        writer.immediateTransaction {
            block()
        }
    }
