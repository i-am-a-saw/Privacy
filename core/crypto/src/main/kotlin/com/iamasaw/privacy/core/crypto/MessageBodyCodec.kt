package com.iamasaw.privacy.core.crypto

import kotlinx.serialization.json.Json

class MessageBodyCodec {
    private val json = Json { ignoreUnknownKeys = true }

    fun encode(body: MessageBody): ByteArray = json.encodeToString(MessageBody.serializer(), body).encodeToByteArray()

    fun decode(payload: ByteArray): MessageBody =
        json.decodeFromString(
            MessageBody.serializer(),
            payload.decodeToString(throwOnInvalidSequence = true),
        )
}
