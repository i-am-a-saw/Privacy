package com.iamasaw.privacy.feature.contacts.qr

import com.iamasaw.privacy.core.common.usernameRule.UsernameRule

private const val MAX_PAYLOAD_LENGTH = 128
private const val PAYLOAD_PART_COUNT = 3

sealed interface QrDecodeResult {
    data class Success(
        val username: String,
    ) : QrDecodeResult

    data object Invalid : QrDecodeResult

    data object UnsupportedVersion : QrDecodeResult
}

object QrPayloadCodec {
    private val versionPattern = Regex("^v[1-9][0-9]*$")

    fun encode(username: String): String {
        require(UsernameRule.isValid(username))
        return "msgr:v1:$username"
    }

    fun decode(payload: String): QrDecodeResult {
        if (payload.length > MAX_PAYLOAD_LENGTH) {
            return QrDecodeResult.Invalid
        }

        val parts = payload.split(':')

        return when {
            parts.size != PAYLOAD_PART_COUNT -> QrDecodeResult.Invalid
            parts[0] != "msgr" -> QrDecodeResult.Invalid
            !versionPattern.matches(parts[1]) -> QrDecodeResult.Invalid
            !UsernameRule.isValid(parts[2]) -> QrDecodeResult.Invalid
            parts[1] != "v1" -> QrDecodeResult.UnsupportedVersion
            else -> QrDecodeResult.Success(parts[2])
        }
    }
}
