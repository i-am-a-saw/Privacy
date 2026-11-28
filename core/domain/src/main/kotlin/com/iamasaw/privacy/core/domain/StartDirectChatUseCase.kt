package com.iamasaw.privacy.core.domain

import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.data.repository.ConversationRepository
import com.iamasaw.privacy.core.model.Conversation
import com.iamasaw.privacy.core.model.ConversationId
import com.iamasaw.privacy.core.model.UserId
import javax.inject.Inject

class StartDirectChatUseCase
    @Inject
    constructor(
        private val conversations: ConversationRepository,
    ) {
        suspend operator fun invoke(peerId: UserId): AppResult<ConversationId> = conversations.startDirectChat(peerId)
    }
