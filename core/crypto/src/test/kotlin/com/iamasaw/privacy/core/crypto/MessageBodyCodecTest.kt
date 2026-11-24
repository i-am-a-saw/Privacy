package com.iamasaw.privacy.core.crypto

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.nio.charset.CharacterCodingException
import java.time.Instant

class MessageBodyCodecTest {
    private val codec = MessageBodyCodec()
    private val clientTime = Instant.parse("2026-01-01T12:00:00.123456789Z")

    @Test
    fun `round trip preserves Unicode and timestamp precision`() {
        val body =
            MessageBody(
                type = MessageBodyType.TEXT,
                text = "Hello, Боб! 👋\n\"Quoted\"",
                clientTime = clientTime,
            )

        assertEquals(body, codec.decode(codec.encode(body)))
    }

    @Test
    fun `encoded body follows the agreed JSON contract`() {
        val body = MessageBody(MessageBodyType.TEXT, "Hello", clientTime)
        val json = Json.parseToJsonElement(codec.encode(body).decodeToString()).jsonObject

        assertEquals(setOf("type", "text", "client_time"), json.keys)
        assertEquals("text", json.getValue("type").jsonPrimitive.content)
        assertEquals("Hello", json.getValue("text").jsonPrimitive.content)
        assertEquals(clientTime.toString(), json.getValue("client_time").jsonPrimitive.content)
    }

    @Test
    fun `unknown additive fields are ignored`() {
        val payload =
            """
            {"type":"text","text":"Hello","client_time":"2026-01-01T12:00:00.123456789Z","extra":true}
            """.trimIndent().encodeToByteArray()

        assertEquals(
            MessageBody(MessageBodyType.TEXT, "Hello", clientTime),
            codec.decode(payload),
        )
    }

    @Test
    fun `unknown message type is rejected`() {
        val payload =
            """
            {"type":"image","text":"Hello","client_time":"2026-01-01T12:00:00Z"}
            """.trimIndent().encodeToByteArray()

        assertThrows(SerializationException::class.java) {
            codec.decode(payload)
        }
    }

    @Test
    fun `missing required timestamp is rejected`() {
        val payload = """{"type":"text","text":"Hello"}""".encodeToByteArray()

        assertThrows(SerializationException::class.java) {
            codec.decode(payload)
        }
    }

    @Test
    fun `malformed JSON is rejected`() {
        assertThrows(SerializationException::class.java) {
            codec.decode("not-json".encodeToByteArray())
        }
    }

    @Test
    fun `invalid client timestamp is rejected`() {
        val payload =
            """
            {"type":"text","text":"Hello","client_time":"tomorrow"}
            """.trimIndent().encodeToByteArray()

        assertThrows(SerializationException::class.java) {
            codec.decode(payload)
        }
    }

    @Test
    fun `invalid UTF-8 is rejected`() {
        assertThrows(CharacterCodingException::class.java) {
            codec.decode(byteArrayOf(0xC3.toByte(), 0x28))
        }
    }

    @Test
    fun `body survives the complete codec and cipher pipeline`() {
        val body = MessageBody(MessageBodyType.TEXT, "Hello", clientTime)
        val registry = CipherRegistry(listOf(NoOpCipher()))
        val cipher = registry.forSuite(CipherSuite.NONE_V0)

        val payload = cipher.encrypt(codec.encode(body))
        val restored = codec.decode(cipher.decrypt(payload))

        assertEquals(body, restored)
    }
}
