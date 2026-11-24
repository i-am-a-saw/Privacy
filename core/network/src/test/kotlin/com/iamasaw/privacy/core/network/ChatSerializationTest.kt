package com.iamasaw.privacy.core.network

import com.iamasaw.privacy.core.network.dto.ByteaSerializer
import com.iamasaw.privacy.core.network.dto.MessageDto
import com.iamasaw.privacy.core.network.realtime.reconnectDelayMillis
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ChatSerializationTest {
    @Test
    fun byteaEncodingUsesPostgresHex() {
        val encoded =
            Json.encodeToString(
                ByteaSerializer,
                byteArrayOf(0, 15, 16, 127, -128, -1),
            )

        assertEquals("\"\\\\x000f107f80ff\"", encoded)
    }

    @Test
    fun byteaDecodingRestoresEveryByte() {
        val decoded =
            Json.decodeFromString(
                ByteaSerializer,
                "\"\\\\x000f107f80ff\"",
            )

        assertArrayEquals(
            byteArrayOf(0, 15, 16, 127, -128, -1),
            decoded,
        )
    }

    @Test
    fun byteaRejectsMissingPrefix() {
        assertThrows(SerializationException::class.java) {
            Json.decodeFromString(ByteaSerializer, "\"000f\"")
        }
    }

    @Test
    fun byteaRejectsOddLengthAndInvalidDigits() {
        listOf("\\x0", "\\xgg", "\\x-1").forEach { invalid ->
            assertThrows(SerializationException::class.java) {
                Json.decodeFromString(
                    ByteaSerializer,
                    Json.encodeToString(invalid),
                )
            }
        }
    }

    @Test
    fun messagePreservesServerTimestampPrecision() {
        val message =
            Json.decodeFromString<MessageDto>(
                """
                {
                  "id":"message",
                  "conversation_id":"conversation",
                  "sender_id":"sender",
                  "payload":"\\x00ff",
                  "cipher_suite":"test",
                  "created_at":"2026-11-24T12:00:00.123456+00:00"
                }
                """.trimIndent(),
            )

        assertEquals(
            "2026-11-24T12:00:00.123456+00:00",
            message.createdAt,
        )
        assertArrayEquals(byteArrayOf(0, -1), message.payload)
    }

    @Test
    fun reconnectDelayIncreasesAndStopsAtThirtySeconds() {
        val actual = (0L..7L).map(::reconnectDelayMillis)

        assertEquals(
            listOf(
                1000L,
                2000L,
                4000L,
                8000L,
                16000L,
                30000L,
                30000L,
                30000L,
            ),
            actual,
        )
    }
}
