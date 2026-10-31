package com.iamasaw.privacy.core.model

import com.iamasaw.privacy.core.model.MessageStatus.DELIVERED
import com.iamasaw.privacy.core.model.MessageStatus.FAILED
import com.iamasaw.privacy.core.model.MessageStatus.PENDING
import com.iamasaw.privacy.core.model.MessageStatus.READ
import com.iamasaw.privacy.core.model.MessageStatus.SENT
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageStatusTest {
    @Test
    fun `pending can become sent`() {
        assertTrue(PENDING.canTransitionTo(SENT))
    }

    @Test
    fun `pending can become failed`() {
        assertTrue(PENDING.canTransitionTo(FAILED))
    }

    @Test
    fun `failed can go back to pending`() {
        assertTrue(FAILED.canTransitionTo(PENDING))
    }

    @Test
    fun `read can change to nothing`() {
        MessageStatus.entries.forEach { next ->
            assertFalse("READ -> $next", READ.canTransitionTo(next))
        }
    }

    @Test
    fun `sent cannot go back to pending`() {
        assertFalse(SENT.canTransitionTo(PENDING))
    }

    @Test
    fun `failed cannot jump straight to sent`() {
        assertFalse(FAILED.canTransitionTo(SENT))
    }

    @Test
    fun `a status cannot change to itself`() {
        MessageStatus.entries.forEach { status ->
            assertFalse("$status -> $status", status.canTransitionTo(status))
        }
    }

    @Test
    fun `every pair matches the transition table`() {
        val allowed =
            listOf(
                PENDING to SENT,
                PENDING to FAILED,
                SENT to DELIVERED,
                SENT to READ,
                DELIVERED to READ,
                FAILED to PENDING,
            )

        for (initialStatus in MessageStatus.entries) {
            for (finalStatus in MessageStatus.entries) {
                assertEquals(
                    "$initialStatus -> $finalStatus",
                    (initialStatus to finalStatus) in allowed,
                    initialStatus.canTransitionTo(finalStatus),
                )
            }
        }
    }
}
