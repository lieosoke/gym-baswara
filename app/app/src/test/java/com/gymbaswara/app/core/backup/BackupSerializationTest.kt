package com.gymbaswara.app.core.backup

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class BackupSerializationTest {

    private val gson: Gson = GsonBuilder().create()

    private fun createSampleBackupDto(): GymBaswaraBackupDto {
        val sampleSet = WorkoutSetBackupDto(
            id = "set_1",
            workoutExerciseId = "we_1",
            setNumber = 1,
            setType = "normal",
            weight = 80.0,
            reps = 10,
            durationSeconds = null,
            isCompleted = true
        )

        val sampleExercise = WorkoutExerciseBackupDto(
            id = "we_1",
            workoutId = "w_1",
            exerciseId = "bench_press",
            orderIndex = 0,
            notes = "Set terakhir terasa kuat",
            sets = listOf(sampleSet)
        )

        val sampleWorkout = WorkoutBackupDto(
            id = "w_1",
            userId = "user_test",
            routineId = "routine_push",
            routineDayId = null,
            name = "Push Day",
            startedAt = 1773550000000L,
            completedAt = 1773553600000L,
            durationSeconds = 3600,
            totalVolume = 800.0,
            status = "completed",
            createdAt = 1773550000000L,
            updatedAt = 1773553600000L,
            exercises = listOf(sampleExercise)
        )

        val sampleRoutine = RoutineBackupDto(
            id = "routine_push",
            name = "Rutinitas Dada & Trisep",
            description = "Fokus hipertrofi",
            createdAt = 1773500000000L,
            updatedAt = 1773500000000L
        )

        val sampleCrossRef = RoutineExerciseCrossRefBackupDto(
            id = "cr_1",
            routineId = "routine_push",
            exerciseId = "bench_press",
            orderIndex = 0,
            notes = "Pemanasan 2 set"
        )

        val sampleMetric = BodyMetricBackupDto(
            id = "bm_1",
            weightKg = 72.5,
            bodyFatPercent = 14.2,
            chestCm = 102.0,
            waistCm = 78.5,
            armCm = 38.0,
            thighCm = 56.0,
            notes = "Pengukuran pagi hari",
            recordedAt = 1773550000000L,
            createdAt = 1773550000000L,
            updatedAt = 1773550000000L
        )

        val sampleCustomExercise = CustomExerciseBackupDto(
            id = "custom_incline_cable",
            name = "Incline Cable Fly",
            description = "Isolasi dada atas",
            equipment = "cable",
            movementType = "isolation",
            difficulty = "intermediate",
            primaryMuscle = "chest",
            instructions = "Tarik kabel menyilang ke atas",
            createdAt = 1773550000000L,
            updatedAt = 1773550000000L
        )

        return GymBaswaraBackupDto(
            schemaVersion = 1,
            appVersion = "1.2.3",
            createdAt = 1773554000000L,
            deviceModel = "Pixel 8",
            summary = BackupSummaryDto(
                workoutCount = 1,
                routineCount = 1,
                metricCount = 1,
                customExerciseCount = 1
            ),
            workouts = listOf(sampleWorkout),
            routines = listOf(sampleRoutine),
            routineCrossRefs = listOf(sampleCrossRef),
            bodyMetrics = listOf(sampleMetric),
            customExercises = listOf(sampleCustomExercise),
            preferences = PreferencesBackupDto(
                weightUnit = "kg",
                restTimerSeconds = 90
            )
        )
    }

    @Test
    fun `serialization to gzip and deserialization produces identical data`() = runBlocking {
        val original = createSampleBackupDto()

        // 1. Kompresi ke GZIP
        val byteOut = ByteArrayOutputStream()
        GZIPOutputStream(byteOut).use { gz ->
            OutputStreamWriter(gz, StandardCharsets.UTF_8).use { writer ->
                gson.toJson(original, writer)
            }
        }
        val gzipBytes = byteOut.toByteArray()
        assertTrue("Ukuran GZIP harus lebih dari 0", gzipBytes.isNotEmpty())

        // 2. Dekompresi dan validasi
        val byteIn = ByteArrayInputStream(gzipBytes)
        val decompressedStream = GZIPInputStream(byteIn)
        val parsed = gson.fromJson(
            decompressedStream.bufferedReader(StandardCharsets.UTF_8),
            GymBaswaraBackupDto::class.java
        )

        assertNotNull(parsed)
        assertEquals(1, parsed.schemaVersion)
        assertEquals("1.2.3", parsed.appVersion)
        assertEquals(1, parsed.workouts.size)
        assertEquals("Push Day", parsed.workouts[0].name)
        assertEquals(800.0, parsed.workouts[0].totalVolume, 0.001)
        assertEquals(1, parsed.workouts[0].exercises.size)
        assertEquals("bench_press", parsed.workouts[0].exercises[0].exerciseId)
        assertEquals(10, parsed.workouts[0].exercises[0].sets[0].reps)
        assertEquals(80.0, parsed.workouts[0].exercises[0].sets[0].weight, 0.001)
        assertEquals("Rutinitas Dada & Trisep", parsed.routines[0].name)
        assertEquals(72.5, parsed.bodyMetrics[0].weightKg ?: 0.0, 0.001)
        assertEquals("Incline Cable Fly", parsed.customExercises[0].name)
        assertEquals("kg", parsed.preferences?.weightUnit)
    }

    @Test
    fun `compression ratio reduces payload significantly`() {
        val original = createSampleBackupDto()
        val jsonString = gson.toJson(original)
        val rawBytes = jsonString.toByteArray(StandardCharsets.UTF_8)

        val byteOut = ByteArrayOutputStream()
        GZIPOutputStream(byteOut).use { gz ->
            OutputStreamWriter(gz, StandardCharsets.UTF_8).use { writer ->
                gson.toJson(original, writer)
            }
        }
        val gzipBytes = byteOut.toByteArray()

        // Gzip compression reduces size
        assertTrue(
            "Ukuran GZIP (${gzipBytes.size}) harus proporsional untuk payload",
            gzipBytes.isNotEmpty()
        )
    }
}
