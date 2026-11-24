package com.iamasaw.privacy.core.crypto

class CipherRegistry(
    ciphers: List<MessageCipher>,
) {
    private val ciphersBySuite = ciphers.associateBy { it.suite }

    init {
        require(ciphersBySuite.size == ciphers.size) {
            "Duplicate cipher suite registrations"
        }
    }

    fun forSuite(suite: CipherSuite): MessageCipher =
        requireNotNull(ciphersBySuite[suite]) {
            "Unsupported cipher suite: ${suite.value}"
        }
}
