package com.iamasaw.privacy.feature.contacts.qr

import com.iamasaw.privacy.core.common.usernameRule.UsernameRule

private const val MAX_PAYLOAD_LENGTH = 128

sealed interface QrDecodeResult {
    data class Success(val username: String) : QrDecodeResult

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

        val parts = payload.split(":")
        if (parts.size != 3 || parts[0] != "msgr") {
            return QrDecodeResult.Invalid
        }

        val version = parts[1]
        val username = parts[2]

        if (!versionPattern.matches(version) || !UsernameRule.isValid(username)) {
            return QrDecodeResult.Invalid
        }

        return if (version == "v1") {
            QrDecodeResult.Success(username)
        } else {
            QrDecodeResult.UnsupportedVersion
        }
    }
}
