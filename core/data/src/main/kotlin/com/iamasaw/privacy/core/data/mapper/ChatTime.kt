package com.iamasaw.privacy.core.data.mapper

import java.time.Instant

private const val MICROS_PER_SECOND = 1_000_000L
private const val NANOS_PER_MICRO = 1_000L

internal fun Instant.toEpochMicros(): Long =
    Math.addExact(
        Math.multiplyExact(epochSecond, MICROS_PER_SECOND),
        nano / NANOS_PER_MICRO,
    )

internal fun Long.toInstantFromMicros(): Instant =
    Instant.ofEpochSecond(
        Math.floorDiv(this, MICROS_PER_SECOND),
        Math.floorMod(this, MICROS_PER_SECOND) * NANOS_PER_MICRO,
    )
