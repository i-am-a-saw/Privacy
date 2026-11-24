package com.iamasaw.privacy.core.crypto

import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test

class CipherRegistryTest {
    @Test
    fun `known suite selects its registered cipher`() {
        val cipher = NoOpCipher()
        val registry = CipherRegistry(listOf(cipher))

        assertSame(cipher, registry.forSuite(CipherSuite("none-v0")))
    }

    @Test
    fun `unknown suite is rejected instead of falling back to no-op`() {
        val registry = CipherRegistry(listOf(NoOpCipher()))

        assertThrows(IllegalArgumentException::class.java) {
            registry.forSuite(CipherSuite("unknown-v1"))
        }
    }

    @Test
    fun `duplicate suite registrations are rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            CipherRegistry(listOf(NoOpCipher(), NoOpCipher()))
        }
    }
}
