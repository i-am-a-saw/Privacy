package com.iamasaw.privacy.core.crypto

interface MessageCipher {
    val suite: CipherSuite

    fun encrypt(plaintext: ByteArray): ByteArray

    fun decrypt(payload: ByteArray): ByteArray
}
