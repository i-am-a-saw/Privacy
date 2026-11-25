package com.iamasaw.privacy.core.data.mapper

import com.iamasaw.privacy.core.crypto.CipherRegistry
import com.iamasaw.privacy.core.crypto.CipherSuite
import com.iamasaw.privacy.core.crypto.MessageBody
import com.iamasaw.privacy.core.crypto.MessageBodyCodec
import com.iamasaw.privacy.core.crypto.MessageBodyType
import com.iamasaw.privacy.core.database.entity.MessageEntity
import com.iamasaw.privacy.core.model.MessageStatus
import com.iamasaw.privacy.core.model.UserId
import com.iamasaw.privacy.core.network.dto.MessageDto
import com.iamasaw.privacy.core.network.dto.MessageInsertDto
import java.time.Instant
import javax.inject.Inject

internal class MessagePayloadMapper
    @Inject
    constructor(
        private val codec: MessageBodyCodec,
        private val ciphers: CipherRegistry,
    ) {
        fun outgoing(message: MessageEntity): MessageInsertDto {
            val cipher = ciphers.forSuite(CipherSuite.NONE_V0)

            val body =
                MessageBody(
                    type = MessageBodyType.TEXT,
                    text = message.text,
                    clientTime = message.localCreatedAtMicros.toInstantFromMicros(),
                )

            return MessageInsertDto(
                id = message.id,
                conversationId = message.conversationId,
                senderId = message.senderId,
                payload = cipher.encrypt(codec.encode(body)),
                cipherSuite = cipher.suite.value,
            )
        }

        fun incoming(
            message: MessageDto,
            owner: UserId,
        ): MessageEntity {
            val cipher = ciphers.forSuite(CipherSuite(message.cipherSuite))
            val body = codec.decode(cipher.decrypt((message.payload)))

            require(body.type == MessageBodyType.TEXT)

            return MessageEntity(
                id = message.id,
                conversationId = message.conversationId,
                senderId = message.senderId,
                text = body.text,
                localCreatedAtMicros = body.clientTime.toEpochMicros(),
                serverCreatedAtMicros = Instant.parse(message.createdAt).toEpochMicros(),
                status = MessageStatus.SENT.name,
                isUnread = message.senderId != owner.value,
            )
        }
    }
