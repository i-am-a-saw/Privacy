package com.iamasaw.privacy.core.common.time

import java.time.Instant

interface Clock {
    fun now(): Instant
}

class DefaultClock : Clock {
    override fun now(): Instant = Instant.now()
}