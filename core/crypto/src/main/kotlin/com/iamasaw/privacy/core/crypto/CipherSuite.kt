package com.iamasaw.privacy.core.crypto

@JvmInline
value class CipherSuite(
    val value: String,
) {
    companion object {
        val NONE_V0 = CipherSuite("none-v0")
    }
}
