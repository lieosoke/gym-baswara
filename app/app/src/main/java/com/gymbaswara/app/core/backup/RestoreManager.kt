package com.gymbaswara.app.core.backup

import androidx.room.withTransaction
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.gymbaswara.app.core.database.GymBaswaraDatabase
import com.gymbaswara.app.core.database.dao.BodyMetricDao
import com.gymbaswara.app.core.database.dao.ExerciseDao
import com.gymbaswara.app.core.database.dao.RoutineDao
import com.gymbaswara.app.core.database.dao.WorkoutDao
import com.gymbaswara.app.core.database.entity.BodyMetricEntity
import com.gymbaswara.app.core.database.entity.ExerciseEntity
import com.gymbaswara.app.core.database.entity.RoutineEntity
import com.gymbaswara.app.core.database.entity.RoutineExerciseCrossRef
import com.gymbaswara.app.core.database.entity.WorkoutEntity
import com.gymbaswara.app.core.database.entity.WorkoutExerciseEntity
import com.gymbaswara.app.core.database.entity.WorkoutSetEntity
import com.gymbaswara.app.core.datastore.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.InputStreamReader
import java.io.PushbackInputStream
import java.nio.charset.StandardCharsets
import java.util.zip.GZIPInputStream
import javax.inject.Inject
import javax.inject.Singleton

data class RestoreResult(
    val workoutCount: Int,
    val routineCount: Int,
    val metricCount: Int,
    val customExerciseCount: Int,
    val backupDate: Long
)

@Singleton
class RestoreManager @Inject constructor(
    private val database: GymBaswaraDatabase,
    private val workoutDao: WorkoutDao,
    private val routineDao: RoutineDao,
    private val bodyMetricDao: BodyMetricDao,
    private val exerciseDao: ExerciseDao,
    private val preferencesManager: PreferencesManager
) {
    private val gson: Gson = GsonBuilder().create()

    suspend fun parseBackup(inputStream: InputStream): Result<GymBaswaraBackupDto> =
        withContext(Dispatchers.IO) {
            try {
                // Mendeteksi apakah stream terkompresi GZIP secara otomatis (magic bytes 0x1f, 0x8b)
                val pushbackStream = PushbackInputStream(inputStream, 2)
                val signature = ByteArray(2)
                val bytesRead = pushbackStream.read(signature)
                if (bytesRead > 0) {
                    pushbackStream.unread(signature, 0, bytesRead)
                }

                val isGzip = bytesRead == 2 &&
                        signature[0] == 0x1f.toByte() &&
                        signature[1] == 0x8b.toByte()

                val effectiveStream = if (isGzip) GZIPInputStream(pushbackStream) else pushbackStream
                val reader = InputStreamReader(effectiveStream, StandardCharsets.UTF_8)
                val backup = gson.fromJson(reader, GymBaswaraBackupDto::class.java)

                if (backup == null || backup.schemaVersion < 1) {
                    Result.failure(IllegalArgumentException("Format file cadangan tidak valid atau rusak."))
                } else {
                    Result.success(backup)
                }
            } catch (e: Exception) {
                Result.failure(IllegalArgumentException("Gagal membaca file cadangan: ${e.localizedMessage ?: "Format tidak dikenal"}", e))
            }
        }

    suspend fun restoreBackup(backup: GymBaswaraBackupDto): Result<RestoreResult> =
        withContext(Dispatchers.IO) {
            try {
                // Seluruh proses restore dibungkus dalam single Room transaction atomik
                database.withTransaction {
                    // 1. Pulihkan latihan kustom (tanpa mengganggu katalog latihan sistem)
                    if (backup.customExercises.isNotEmpty()) {
                        val customEntities = backup.customExercises.map { ex ->
                            ExerciseEntity(
                                id = ex.id,
                                name = ex.name,
                                description = ex.description,
                                equipment = ex.equipment,
                                movementType = ex.movementType,
                                difficulty = ex.difficulty,
                                primaryMuscle = ex.primaryMuscle,
                                instructions = ex.instructions,
                                imageUrl = null,
                                isSystem = false,
                                createdAt = ex.createdAt,
                                updatedAt = ex.updatedAt
                            )
                        }
                        exerciseDao.insertExercises(customEntities)
                    }

                    // 2. Pulihkan Rutinitas & Cross References
                    if (backup.routines.isNotEmpty()) {
                        val routineEntities = backup.routines.map { r ->
                            RoutineEntity(
                                id = r.id,
                                name = r.name,
                                description = r.description,
                                createdAt = r.createdAt,
                                updatedAt = r.updatedAt,
                                syncStatus = "SYNCED"
                            )
                        }
                        routineDao.insertRoutines(routineEntities)
                    }

                    if (backup.routineCrossRefs.isNotEmpty()) {
                        val crossRefEntities = backup.routineCrossRefs.map { cr ->
                            RoutineExerciseCrossRef(
                                id = cr.id,
                                routineId = cr.routineId,
                                exerciseId = cr.exerciseId,
                                orderIndex = cr.orderIndex,
                                notes = cr.notes
                            )
                        }
                        routineDao.insertRoutineExercises(crossRefEntities)
                    }

                    // 3. Pulihkan Sesi Latihan, Latihan Sesi, dan Set Latihan
                    val workoutEntities = mutableListOf<WorkoutEntity>()
                    val workoutExerciseEntities = mutableListOf<WorkoutExerciseEntity>()
                    val workoutSetEntities = mutableListOf<WorkoutSetEntity>()

                    backup.workouts.forEach { w ->
                        workoutEntities.add(
                            WorkoutEntity(
                                id = w.id,
                                userId = w.userId ?: "offline_user",
                                routineId = w.routineId,
                                routineDayId = w.routineDayId,
                                name = w.name,
                                startedAt = w.startedAt,
                                completedAt = w.completedAt,
                                durationSeconds = w.durationSeconds,
                                totalVolume = w.totalVolume,
                                status = w.status,
                                syncStatus = "SYNCED",
                                createdAt = w.createdAt,
                                updatedAt = w.updatedAt
                            )
                        )

                        w.exercises.forEach { ex ->
                            workoutExerciseEntities.add(
                                WorkoutExerciseEntity(
                                    id = ex.id,
                                    workoutId = ex.workoutId,
                                    exerciseId = ex.exerciseId,
                                    orderIndex = ex.orderIndex,
                                    supersetId = null,
                                    notes = ex.notes
                                )
                            )

                            ex.sets.forEach { s ->
                                workoutSetEntities.add(
                                    WorkoutSetEntity(
                                        id = s.id,
                                        workoutExerciseId = s.workoutExerciseId,
                                        setNumber = s.setNumber,
                                        setType = s.setType,
                                        weight = s.weight,
                                        reps = s.reps,
                                        durationSeconds = s.durationSeconds,
                                        distance = null,
                                        rpe = null,
                                        rir = null,
                                        isCompleted = s.isCompleted,
                                        completedAt = null
                                    )
                                )
                            }
                        }
                    }

                    if (workoutEntities.isNotEmpty()) {
                        workoutDao.insertWorkouts(workoutEntities)
                    }
                    if (workoutExerciseEntities.isNotEmpty()) {
                        workoutDao.insertWorkoutExercises(workoutExerciseEntities)
                    }
                    if (workoutSetEntities.isNotEmpty()) {
                        workoutDao.insertWorkoutSets(workoutSetEntities)
                    }

                    // 4. Pulihkan Ukuran Tubuh
                    if (backup.bodyMetrics.isNotEmpty()) {
                        val metricEntities = backup.bodyMetrics.map { m ->
                            BodyMetricEntity(
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
                                updatedAt = m.updatedAt,
                                syncStatus = "SYNCED"
                            )
                        }
                        bodyMetricDao.upsertAll(metricEntities)
                    }
                }

                // 5. Pulihkan Preferensi Pengguna
                backup.preferences?.let { prefs ->
                    preferencesManager.setWeightUnit(prefs.weightUnit)
                    preferencesManager.setRestTimerSeconds(prefs.restTimerSeconds)
                }

                Result.success(
                    RestoreResult(
                        workoutCount = backup.workouts.size,
                        routineCount = backup.routines.size,
                        metricCount = backup.bodyMetrics.size,
                        customExerciseCount = backup.customExercises.size,
                        backupDate = backup.createdAt
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
