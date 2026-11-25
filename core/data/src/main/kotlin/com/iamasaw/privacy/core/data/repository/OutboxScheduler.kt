package com.iamasaw.privacy.core.data.repository

import com.iamasaw.privacy.core.model.ConversationId

interface OutboxScheduler {
    fun schedule(conversationId: ConversationId)
}
