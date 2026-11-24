package com.iamasaw.privacy.core.network.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Suppress("MagicNumber")
object ByteaSerializer : KSerializer<ByteArray> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("PostgresBytea", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: ByteArray,
    ) {
        val hex =
            value.joinToString("") { byte ->
                (byte.toInt() and 0xff).toString(16).padStart(2, '0')
            }
        encoder.encodeString("\\x$hex")
    }

    override fun deserialize(decoder: Decoder): ByteArray {
        val value = decoder.decodeString()
        if (!value.startsWith("\\x") || (value.length - 2) % 2 != 0) {
            throw SerializationException("Expected PostgreSQL Bytea hex")
        }

        return ByteArray((value.length - 2) / 2) { index ->
            val offset = 2 + index * 2
            val high = value[offset].digitToIntOrNull(16)
            val low = value[offset + 1].digitToIntOrNull(16)

            if (high == null || low == null) {
                throw SerializationException("Invalid PostgreSQL bytea hex")
            }

            ((high shl 4) or low).toByte()
        }
    }
}
