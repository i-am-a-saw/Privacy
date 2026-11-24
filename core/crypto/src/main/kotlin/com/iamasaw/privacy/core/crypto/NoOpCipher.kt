package com.iamasaw.privacy.core.crypto

// Development only: payload remain readable text
class NoOpCipher : MessageCipher {
    override val suite: CipherSuite = CipherSuite.NONE_V0

    override fun encrypt(plaintext: ByteArray) = plaintext.copyOf()

    override fun decrypt(payload: ByteArray) = payload.copyOf()
}
