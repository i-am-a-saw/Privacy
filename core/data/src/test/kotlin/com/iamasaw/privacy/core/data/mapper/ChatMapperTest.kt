package com.iamasaw.privacy.core.data.mapper

import com.iamasaw.privacy.core.crypto.CipherRegistry
import com.iamasaw.privacy.core.crypto.MessageBodyCodec
import com.iamasaw.privacy.core.crypto.NoOpCipher
import com.iamasaw.privacy.core.data.repository.isPermanentSendStatus
import com.iamasaw.privacy.core.database.entity.MessageEntity
import com.iamasaw.privacy.core.model.MessageStatus
import com.iamasaw.privacy.core.model.UserId
import com.iamasaw.privacy.core.network.dto.MessageDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class ChatMapperTest {
    private val payloads =
        MessagePayloadMapper(
            MessageBodyCodec(),
            CipherRegistry(listOf(NoOpCipher())),
        )

    @Test
    fun timestampRoundTripPreservesMicroseconds() {
        val instant = Instant.parse("2026-11-25T12:00:00.123456Z")

        assertEquals(
            instant,
            instant.toEpochMicros().toInstantFromMicros(),
        )
    }

    @Test
    fun timestampBeforeEpochRoundTrips() {
        val instant = Instant.parse("1969-12-31T23:59:59.999999Z")

        assertEquals(-1L, instant.toEpochMicros())
        assertEquals(instant, (-1L).toInstantFromMicros())
    }

    @Test
    fun messagePassesThroughCodecAndCipher() {
        val original = row()
        val request = payloads.outgoing(original)

        val decoded =
            payloads.incoming(
                MessageDto(
                    request.id,
                    request.conversationId,
                    request.senderId,
                    request.payload,
                    request.cipherSuite,
                    "2026-11-25T12:00:01.123456Z",
                ),
                UserId("bob"),
            )

        assertEquals(original.id, decoded.id)
        assertEquals(original.text, decoded.text)
        assertEquals(
            original.localCreatedAtMicros,
            decoded.localCreatedAtMicros,
        )
        assertEquals(MessageStatus.SENT.name, decoded.status)
        assertTrue(decoded.isUnread)
    }

    @Test
    fun duplicateDoesNotRestoreUnreadOrDowngradeReadStatus() {
        val existing =
            row().copy(
                status = MessageStatus.READ.name,
                isUnread = false,
            )

        val incoming =
            row().copy(
                status = MessageStatus.SENT.name,
                isUnread = true,
                localCreatedAtMicros = 999L,
            )

        val merged = incoming.mergeExisting(existing)

        assertFalse(merged.isUnread)
        assertEquals(MessageStatus.READ.name, merged.status)
        assertEquals(
            existing.localCreatedAtMicros,
            merged.localCreatedAtMicros,
        )
    }

    @Test
    fun ownMessageIsNotUnread() {
        val request = payloads.outgoing(row())

        val response =
            MessageDto(
                request.id,
                request.conversationId,
                request.senderId,
                request.payload,
                request.cipherSuite,
                "2026-11-25T12:00:01Z",
            )

        assertFalse(
            payloads.incoming(response, UserId("alice")).isUnread,
        )
    }

    @Test
    fun sendPolicyDistinguishesRetryFromPermanentRejection() {
        listOf(400, 403, 404, 409, 422).forEach {
            assertTrue(isPermanentSendStatus(it))
        }

        listOf(200, 401, 408, 425, 429, 500, 503).forEach {
            assertFalse(isPermanentSendStatus(it))
        }
    }

    private fun row(): MessageEntity =
        MessageEntity(
            id = "message",
            conversationId = "conversation",
            senderId = "alice",
            text = "Hello",
            localCreatedAtMicros =
                Instant
                    .parse("2026-11-25T12:00:00.123456Z")
                    .toEpochMicros(),
        )
}
