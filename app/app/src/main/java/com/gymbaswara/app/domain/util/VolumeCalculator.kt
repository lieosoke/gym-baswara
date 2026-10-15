package com.gymbaswara.app.domain.util

import com.gymbaswara.app.domain.model.WorkoutSet
import java.util.Calendar

/**
 * Single source of truth untuk perhitungan volume beban.
 *
 * Aturan:
 * - Volume per set = weight (kg) x reps, hanya untuk set yang completed.
 * - Set yang di-skip / tidak completed TIDAK dihitung.
 * - Bodyweight set (weight == 0) menyumbang 0 ke volume beban; catat durasi/
 *   TUT secara terpisah bila tersedia. Jangan pernah mengarang berat badan user.
 * - Set isometrik/time-based (reps == 0, durationSeconds > 0) menyumbang 0 ke
 *   volume beban; agregasikan sebagai total time-under-tension (detik).
 * - Warm-up: flag isWarmup; default disertakan, bisa dipisah via [includeWarmup].
 * - Semua batas periode memakai waktu lokal user (Calendar.getInstance()).
 */
object VolumeCalculator {

    fun setVolume(
        weightKg: Double,
        reps: Int,
        isCompleted: Boolean,
        isWarmup: Boolean = false,
        includeWarmup: Boolean = true
    ): Double {
        if (!isCompleted) return 0.0
        if (isWarmup && !includeWarmup) return 0.0
        if (weightKg <= 0.0 || reps <= 0) return 0.0
        return weightKg * reps
    }

    fun sessionVolume(
        sets: List<WorkoutSet>,
        includeWarmup: Boolean = true,
        isWarmup: (WorkoutSet) -> Boolean = { false }
    ): Double {
        var total = 0.0
        for (set in sets) {
            total += setVolume(
                weightKg = set.weight,
                reps = set.reps,
                isCompleted = set.isCompleted,
                isWarmup = isWarmup(set),
                includeWarmup = includeWarmup
            )
        }
        return total
    }

    fun timeUnderTensionSeconds(sets: List<WorkoutSet>): Int {
        return sets
            .filter { it.isCompleted && it.reps == 0 }
            .sumOf { it.durationSeconds ?: 0 }
    }

    fun getStartOfWeekMonday(nowMillis: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    fun getStartOfMonth(nowMillis: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    fun getStartOfYear(nowMillis: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    const val FORMULA_EXPLANATION =
        "Volume per set = beban (kg) x reps. Hanya set yang ditandai selesai yang " +
            "dihitung. Set bodyweight dan isometrik (plank, dead hang) tidak masuk " +
            "volume beban dan dicatat terpisah sebagai time-under-tension. " +
            "Volume sesi = jumlah volume semua set selesai. " +
            "Volume mingguan/bulanan/tahunan = jumlah volume sesi pada periode " +
            "tersebut (awal minggu: Senin, waktu lokal)."
}
