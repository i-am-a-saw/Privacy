package com.iamasaw.privacy.core.testing.data

import com.iamasaw.privacy.core.model.Conversation
import com.iamasaw.privacy.core.model.ConversationId
import com.iamasaw.privacy.core.model.Message
import com.iamasaw.privacy.core.model.MessageContent
import com.iamasaw.privacy.core.model.MessageId
import com.iamasaw.privacy.core.model.MessageStatus
import com.iamasaw.privacy.core.model.User
import com.iamasaw.privacy.core.model.UserId
import java.time.Instant

fun testUser(
    id: String = "user-alice",
    username: String = "alice",
    displayName: String = "Alice",
    avatarPath: String? = null,
) = User(
    id = UserId(id),
    username = username,
    displayName = displayName,
    avatarPath = avatarPath,
)

fun testConversation(
    id: String = "conversation-1",
    peer: User = testUser(id = "user-bob", username = "bob", displayName = "Bob"),
    lastMessagePreview: String? = null,
    lastMessageAt: Instant? = null,
    unreadCount: Int = 0,
) = Conversation(
    id = ConversationId(id),
    peer = peer,
    lastMessagePreview = lastMessagePreview,
    lastMessageAt = lastMessageAt,
    unreadCount = unreadCount,
)

fun testMessage(
    id: String = "message-1",
    text: String = "Hello",
    status: MessageStatus = MessageStatus.PENDING,
    senderId: String = "user-alice",
    conversationId: String = "conversation-1",
) = Message(
    id = MessageId(id),
    status = status,
    content = MessageContent.Text(text),
    senderId = UserId(senderId),
    conversationId = ConversationId(conversationId),
    localCreatedAt = Instant.parse("2026-01-01T12:00:00Z"),
    serverCreatedAt = null,
)
