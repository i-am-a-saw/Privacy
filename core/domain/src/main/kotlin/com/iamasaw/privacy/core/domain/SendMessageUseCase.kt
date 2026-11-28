package com.iamasaw.privacy.core.domain

import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.data.repository.MessageRepository
import com.iamasaw.privacy.core.model.ConversationId
import com.iamasaw.privacy.core.model.MessageId
import javax.inject.Inject

class SendMessageUseCase
    @Inject
    constructor(
        private val messages: MessageRepository,
    ) {
        suspend operator fun invoke(
            conversationId: ConversationId,
            text: String,
        ): AppResult<MessageId> = messages.enqueueOutgoing(conversationId, text)
    }
