package com.iamasaw.privacy.core.data.repository

import androidx.paging.PagingData
import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.model.ConversationId
import com.iamasaw.privacy.core.model.Message
import com.iamasaw.privacy.core.model.MessageId
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun observeMessages(conversationId: ConversationId): Flow<PagingData<Message>>

    suspend fun enqueueOutgoing(
        conversationId: ConversationId,
        text: String,
    ): AppResult<MessageId>

    suspend fun sendPending(messageId: MessageId): SendPendingResult

    suspend fun syncInbox(): AppResult<Unit>
}
