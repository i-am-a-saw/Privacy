package com.iamasaw.privacy.core.crypto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNotSame
import org.junit.Test

class NoOpCipherTest {
    private val cipher = NoOpCipher()

    @Test
    fun `encrypt preserves bytes without sharing the input buffer`() {
        val input = "Hello".encodeToByteArray()
        val result = cipher.encrypt(input)

        assertArrayEquals(input, result)
        assertNotSame(input, result)

        result[0] = 0
        assertArrayEquals("Hello".encodeToByteArray(), input)
    }

    @Test
    fun `decrypt preserves bytes without sharing the input buffer`() {
        val input = "Hello".encodeToByteArray()
        val result = cipher.decrypt(input)

        assertArrayEquals(input, result)
        assertNotSame(input, result)

        result[0] = 0
        assertArrayEquals("Hello".encodeToByteArray(), input)
    }
}
