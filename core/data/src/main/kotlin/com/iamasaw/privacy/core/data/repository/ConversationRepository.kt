package com.iamasaw.privacy.core.data.repository

import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.model.Conversation
import com.iamasaw.privacy.core.model.ConversationId
import com.iamasaw.privacy.core.model.UserId
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    fun observeAll(): Flow<List<Conversation>>

    suspend fun startDirectChat(otherUserId: UserId): AppResult<ConversationId>
}
