package com.iamasaw.privacy.feature.contacts.qr

import org.junit.Assert.assertEquals
import org.junit.Test

class QrPayloadCodecTest {
    @Test
    fun validUsernamesRoundTrip() {
        val usernames = listOf("alice", "bob_123", "a".repeat(20))

        usernames.forEach { username ->
            assertEquals(
                QrDecodeResult.Success(username),
                QrPayloadCodec.decode(QrPayloadCodec.encode(username)),
            )
        }
    }

    @Test
    fun garbagePayloadsAreRejected() {
        val payloads =
            listOf(
                "",
                "alice",
                "https://example.com",
                "other:v1:alice",
                "msgr:v1:",
                "msgr:v1:ALICE",
                "msgr:v1:ab",
                "msgr:v1:alice:extra",
                "msgr:v01:alice",
                "msgr:v1:${"a".repeat(21)}",
                "x".repeat(129),
            )

        payloads.forEach { payload ->
            assertEquals(
                "Unexpected result for $payload",
                QrDecodeResult.Invalid,
                QrPayloadCodec.decode(payload),
            )
        }
    }

    @Test
    fun futureVersionIsReportedSeparately() {
        assertEquals(
            QrDecodeResult.UnsupportedVersion,
            QrPayloadCodec.decode("msgr:v2:alice"),
        )
    }
}
