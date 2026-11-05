package com.albkestrel.launcher.feature.home

import org.junit.Assert.assertEquals
import org.junit.Test

class ClockTickerTest {

    @Test
    fun `waits a full minute at an exact minute boundary`() {
        assertEquals(60_000L, millisUntilNextMinute(0L))
        assertEquals(60_000L, millisUntilNextMinute(60_000L))
        assertEquals(60_000L, millisUntilNextMinute(1_800_000L))
    }

    @Test
    fun `aligns to the remaining time in the current minute`() {
        assertEquals(59_999L, millisUntilNextMinute(1L))
        assertEquals(1_000L, millisUntilNextMinute(59_000L))
        assertEquals(1L, millisUntilNextMinute(59_999L))
    }

    @Test
    fun `never returns zero or a negative delay`() {
        val samples = listOf(0L, 1L, 999L, 59_999L, 60_000L, 123_456_789L)
        samples.forEach { now ->
            val delay = millisUntilNextMinute(now)
            assert(delay in 1..60_000L) { "Unexpected tick delay $delay for $now" }
        }
    }
}
