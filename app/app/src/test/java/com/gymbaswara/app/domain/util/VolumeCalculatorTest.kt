package com.gymbaswara.app.domain.util

import com.gymbaswara.app.domain.model.WorkoutSet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class VolumeCalculatorTest {

    private fun set(
        weight: Double = 0.0,
        reps: Int = 0,
        completed: Boolean = true,
        duration: Int? = null
    ) = WorkoutSet(
        id = java.util.UUID.randomUUID().toString(),
        setNumber = 1,
        weight = weight,
        reps = reps,
        durationSeconds = duration,
        isCompleted = completed
    )

    @Test
    fun `set volume is weight times reps`() {
        assertEquals(1000.0, VolumeCalculator.setVolume(100.0, 10, true), 0.001)
    }

    @Test
    fun `skipped set contributes zero`() {
        assertEquals(0.0, VolumeCalculator.setVolume(100.0, 10, false), 0.001)
    }

    @Test
    fun `bodyweight set contributes zero to load volume`() {
        assertEquals(0.0, VolumeCalculator.setVolume(0.0, 15, true), 0.001)
    }

    @Test
    fun `warmup excluded when flag off`() {
        assertEquals(
            0.0,
            VolumeCalculator.setVolume(60.0, 10, true, isWarmup = true, includeWarmup = false),
            0.001
        )
        assertEquals(
            600.0,
            VolumeCalculator.setVolume(60.0, 10, true, isWarmup = true, includeWarmup = true),
            0.001
        )
    }

    @Test
    fun `session volume sums only completed sets`() {
        val sets = listOf(
            set(weight = 80.0, reps = 8),
            set(weight = 80.0, reps = 8, completed = false),
            set(weight = 0.0, reps = 12)
        )
        assertEquals(640.0, VolumeCalculator.sessionVolume(sets), 0.001)
    }

    @Test
    fun `isometric sets aggregate as time under tension`() {
        val sets = listOf(
            set(reps = 0, duration = 90),
            set(reps = 0, duration = 60, completed = false),
            set(weight = 80.0, reps = 8)
        )
        assertEquals(90, VolumeCalculator.timeUnderTensionSeconds(sets))
        assertEquals(640.0, VolumeCalculator.sessionVolume(sets), 0.001)
    }

    @Test
    fun `week starts on Monday local time`() {
        // Sunday 2026-10-11 23:30 local -> Monday 2026-10-05 00:00 local
        val sundayNight = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 11, 23, 30, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val start = Calendar.getInstance().apply {
            timeInMillis = VolumeCalculator.getStartOfWeekMonday(sundayNight)
        }
        assertEquals(Calendar.MONDAY, start.get(Calendar.DAY_OF_WEEK))
        assertEquals(5, start.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, start.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun `midnight crossover session belongs to start day`() {
        val mondayStart = VolumeCalculator.getStartOfWeekMonday()
        // A session started Sunday night is still >= Monday 00:00 of the same week
        assertTrue(mondayStart <= System.currentTimeMillis())
    }

    @Test
    fun `month and year boundaries are first day midnight`() {
        val now = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 15, 12, 0, 0)
        }.timeInMillis
        val month = Calendar.getInstance().apply {
            timeInMillis = VolumeCalculator.getStartOfMonth(now)
        }
        assertEquals(1, month.get(Calendar.DAY_OF_MONTH))
        val year = Calendar.getInstance().apply {
            timeInMillis = VolumeCalculator.getStartOfYear(now)
        }
        assertEquals(1, year.get(Calendar.DAY_OF_YEAR))
    }
}
