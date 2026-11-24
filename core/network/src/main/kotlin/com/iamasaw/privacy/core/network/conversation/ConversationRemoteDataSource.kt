package com.iamasaw.privacy.core.network.conversation

import com.iamasaw.privacy.core.network.dto.ConversationDto

interface ConversationRemoteDataSource {
    suspend fun startDirectChat(otherUser: String): String

    suspend fun fetchMyConversations(): List<ConversationDto>
}
