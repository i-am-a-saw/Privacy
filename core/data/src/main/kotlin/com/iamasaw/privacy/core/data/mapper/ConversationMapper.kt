package com.iamasaw.privacy.core.data.mapper

import com.iamasaw.privacy.core.database.entity.ConversationSummary
import com.iamasaw.privacy.core.model.Conversation
import com.iamasaw.privacy.core.model.ConversationId
import com.iamasaw.privacy.core.model.User
import com.iamasaw.privacy.core.model.UserId

internal fun ConversationSummary.toConversation(): Conversation = Conversation(
    id = ConversationId(conversation.id),
    peer = User(
        id = UserId(conversation.peerUserId),
        username = conversation.peerUsername,
        displayName = conversation.peerDisplayName,
        avatarPath = conversation.peerAvatarPath
    ),
    lastMessagePreview = lastMessagePreview,
    lastMessageAt = lastMessageAtMicros?.toInstantFromMicros(),
    unreadCount = unreadCount,
)
