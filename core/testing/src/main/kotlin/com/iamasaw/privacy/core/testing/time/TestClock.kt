package com.iamasaw.privacy.core.testing.time

import com.iamasaw.privacy.core.common.time.Clock
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.toJavaDuration

class TestClock(
    private var current: Instant = Instant.parse("2026-01-01T00:00:00Z")
) : Clock {

    override fun now(): Instant = current

    fun advanceBy(duration: Duration) {
        current = current.plus(duration.toJavaDuration())
    }

    fun setTo(instant: Instant) {
        current = instant
    }
}