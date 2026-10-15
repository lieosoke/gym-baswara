package com.gymbaswara.app.core.backup

import android.os.Build
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.gymbaswara.app.BuildConfig
import com.gymbaswara.app.core.database.dao.BodyMetricDao
import com.gymbaswara.app.core.database.dao.ExerciseDao
import com.gymbaswara.app.core.database.dao.RoutineDao
import com.gymbaswara.app.core.database.dao.WorkoutDao
import com.gymbaswara.app.core.datastore.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.zip.GZIPOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val routineDao: RoutineDao,
    private val bodyMetricDao: BodyMetricDao,
    private val exerciseDao: ExerciseDao,
    private val preferencesManager: PreferencesManager
) {
    private val gson: Gson = GsonBuilder().create()

    suspend fun createBackup(): GymBaswaraBackupDto = withContext(Dispatchers.IO) {
        val workoutsWithDetails = workoutDao.getAllWorkoutsWithDetails()
        val routines = routineDao.getAllRoutinesOnce()
        val crossRefs = routineDao.getAllCrossRefsOnce()
        val bodyMetrics = bodyMetricDao.getAllOnce()
        val customExercises = exerciseDao.getCustomExercises()
        val prefs = preferencesManager.preferences.first()

        val workoutDtos = workoutsWithDetails.map { detail ->
            WorkoutBackupDto(
                id = detail.workout.id,
                userId = detail.workout.userId,
                routineId = detail.workout.routineId,
                routineDayId = detail.workout.routineDayId,
                name = detail.workout.name,
                startedAt = detail.workout.startedAt,
                completedAt = detail.workout.completedAt,
                durationSeconds = detail.workout.durationSeconds,
                totalVolume = detail.workout.totalVolume,
                status = detail.workout.status,
                createdAt = detail.workout.createdAt,
                updatedAt = detail.workout.updatedAt,
                exercises = detail.exercises.map { ex ->
                    WorkoutExerciseBackupDto(
                        id = ex.exercise.id,
                        workoutId = ex.exercise.workoutId,
                        exerciseId = ex.exercise.exerciseId,
                        orderIndex = ex.exercise.orderIndex,
                        notes = ex.exercise.notes,
                        sets = ex.sets.map { s ->
                            WorkoutSetBackupDto(
                                id = s.id,
                                workoutExerciseId = s.workoutExerciseId,
                                setNumber = s.setNumber,
                                setType = s.setType,
                                weight = s.weight,
                                reps = s.reps,
                                durationSeconds = s.durationSeconds,
                                isCompleted = s.isCompleted
                            )
                        }
                    )
                }
            )
        }

        val routineDtos = routines.map { r ->
            RoutineBackupDto(
                id = r.id,
                name = r.name,
                description = r.description,
                createdAt = r.createdAt,
                updatedAt = r.updatedAt
            )
        }

        val crossRefDtos = crossRefs.map { cr ->
            RoutineExerciseCrossRefBackupDto(
                id = cr.id,
                routineId = cr.routineId,
                exerciseId = cr.exerciseId,
                orderIndex = cr.orderIndex,
                notes = cr.notes
            )
        }

        val metricDtos = bodyMetrics.map { m ->
            BodyMetricBackupDto(
                id = m.id,
                weightKg = m.weightKg,
                bodyFatPercent = m.bodyFatPercent,
                chestCm = m.chestCm,
                waistCm = m.waistCm,
                armCm = m.armCm,
                thighCm = m.thighCm,
                notes = m.notes,
                recordedAt = m.recordedAt,
                createdAt = m.createdAt,
                updatedAt = m.updatedAt
            )
        }

        val customExerciseDtos = customExercises.map { e ->
            CustomExerciseBackupDto(
                id = e.id,
                name = e.name,
                description = e.description,
                equipment = e.equipment,
                movementType = e.movementType,
                difficulty = e.difficulty,
                primaryMuscle = e.primaryMuscle,
                instructions = e.instructions,
                createdAt = e.createdAt,
                updatedAt = e.updatedAt
            )
        }

        GymBaswaraBackupDto(
            schemaVersion = 1,
            appVersion = BuildConfig.VERSION_NAME,
            createdAt = System.currentTimeMillis(),
            deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
            summary = BackupSummaryDto(
                workoutCount = workoutDtos.size,
                routineCount = routineDtos.size,
                metricCount = metricDtos.size,
                customExerciseCount = customExerciseDtos.size
            ),
            workouts = workoutDtos,
            routines = routineDtos,
            routineCrossRefs = crossRefDtos,
            bodyMetrics = metricDtos,
            customExercises = customExerciseDtos,
            preferences = PreferencesBackupDto(
                weightUnit = prefs.weightUnit,
                restTimerSeconds = prefs.restTimerSeconds
            )
        )
    }

    suspend fun createGzipBackupBytes(): ByteArray = withContext(Dispatchers.IO) {
        val backup = createBackup()
        val byteStream = ByteArrayOutputStream()
        writeBackupToGzipStream(backup, byteStream)
        byteStream.toByteArray()
    }

    suspend fun writeBackupToGzipStream(backup: GymBaswaraBackupDto, outputStream: OutputStream) =
        withContext(Dispatchers.IO) {
            GZIPOutputStream(outputStream).use { gzipStream ->
                OutputStreamWriter(gzipStream, StandardCharsets.UTF_8).use { writer ->
                    gson.toJson(backup, writer)
                }
            }
        }
}
