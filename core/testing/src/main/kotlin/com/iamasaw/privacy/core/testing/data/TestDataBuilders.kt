package com.iamasaw.privacy.core.testing.data

import com.iamasaw.privacy.core.model.User
import com.iamasaw.privacy.core.model.UserId
import com.iamasaw.privacy.core.model.Conversation
import com.iamasaw.privacy.core.model.MessageStatus
import com.iamasaw.privacy.core.model.MessageContent
import com.iamasaw.privacy.core.model.Message
import com.iamasaw.privacy.core.model.ConversationId
import com.iamasaw.privacy.core.model.MessageId

fun testUser(
    id: String = "user-alice",
    username: String = "alice",
    displayName: String = "Alice",
    avatarPath: String? = null
) = User(
    id = UserId(id),
    username = username,
    displayName = displayName,
    avatarPath = avatarPath
)

fun testConversation(
    id: String = "conversation-1",
    users: List<User> = listOf(testUser(), testUser(id = "user-bob", username = "bob", displayName = "Bob")),
) = Conversation(
    id = ConversationId(id),
    users = users,
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
)