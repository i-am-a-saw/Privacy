package com.iamasaw.privacy.core.network.message

import com.iamasaw.privacy.core.network.dto.MessageCursorDto
import com.iamasaw.privacy.core.network.dto.MessageDto
import com.iamasaw.privacy.core.network.dto.MessageInsertDto

interface MessageRemoteDataSource {
    suspend fun insert(message: MessageInsertDto): MessageDto

    suspend fun fetchSince(
        cursor: MessageCursorDto?,
        pageSize: Int = 100,
    ): List<MessageDto>
}
