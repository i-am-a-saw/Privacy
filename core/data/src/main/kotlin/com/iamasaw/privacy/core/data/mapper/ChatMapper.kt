package com.iamasaw.privacy.core.data.mapper

import com.iamasaw.privacy.core.database.entity.ConversationEntity
import com.iamasaw.privacy.core.database.entity.MessageEntity
import com.iamasaw.privacy.core.model.ConversationId
import com.iamasaw.privacy.core.model.Message
import com.iamasaw.privacy.core.model.MessageContent
import com.iamasaw.privacy.core.model.MessageId
import com.iamasaw.privacy.core.model.MessageStatus
import com.iamasaw.privacy.core.model.UserId
import com.iamasaw.privacy.core.network.dto.ConversationDto
import java.time.Instant

internal fun MessageEntity.toMessage(): Message =
    Message(
        id = MessageId(id),
        status = MessageStatus.valueOf(status),
        content = MessageContent.Text(text),
        senderId = UserId(senderId),
        conversationId = ConversationId(conversationId),
        localCreatedAt = localCreatedAtMicros.toInstantFromMicros(),
        serverCreatedAt = serverCreatedAtMicros?.toInstantFromMicros(),
    )

internal fun ConversationDto.toEntity(owner: UserId): ConversationEntity {
    require(type == "direct") {
        "Only direct conversations are supported"
    }

    val peer = members.single { it.userId != owner.value }
    val profile =
        checkNotNull(peer.profile) {
            "Peer profile is missing"
        }

    return ConversationEntity(
        id = id,
        peerUserId = peer.userId,
        peerUsername = profile.username,
        peerDisplayName = profile.displayName ?: profile.username,
        createdAtMicros = Instant.parse(createdAt).toEpochMicros(),
        peerAvatarPath = profile.avatarPath,
    )
}

internal fun MessageEntity.mergeExisting(existing: MessageEntity?): MessageEntity =
    if (existing == null) {
        this
    } else {
        copy(
            localCreatedAtMicros = existing.localCreatedAtMicros,
            isUnread = existing.isUnread,
            status =
                when (existing.status) {
                    MessageStatus.READ.name,
                    MessageStatus.DELIVERED.name,
                    -> existing.status

                    else -> status
                },
        )
    }
