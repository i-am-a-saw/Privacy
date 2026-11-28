package com.iamasaw.privacy.core.domain

import com.iamasaw.privacy.core.data.repository.ConversationRepository
import com.iamasaw.privacy.core.model.Conversation
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveConversationsUseCase
    @Inject
    constructor(
        private val conversations: ConversationRepository,
    ) {
        operator fun invoke(): Flow<List<Conversation>> = conversations.observeAll()
    }
