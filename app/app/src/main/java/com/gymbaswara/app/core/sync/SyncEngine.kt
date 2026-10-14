package com.gymbaswara.app.core.sync

import com.gymbaswara.app.core.database.dao.BodyMetricDao
import com.gymbaswara.app.core.database.dao.RoutineDao
import com.gymbaswara.app.core.database.dao.WorkoutDao
import com.gymbaswara.app.core.database.entity.BodyMetricEntity
import com.gymbaswara.app.core.database.entity.RoutineEntity
import com.gymbaswara.app.core.database.entity.RoutineExerciseCrossRef
import com.gymbaswara.app.core.database.entity.WorkoutEntity
import com.gymbaswara.app.core.database.entity.WorkoutExerciseEntity
import com.gymbaswara.app.core.database.entity.WorkoutSetEntity
import com.gymbaswara.app.core.datastore.PreferencesManager
import com.gymbaswara.app.core.datastore.SessionManager
import com.gymbaswara.app.core.network.BodyMetricDto
import com.gymbaswara.app.core.network.GymBaswaraApi
import com.gymbaswara.app.core.network.RoutineDto
import com.gymbaswara.app.core.network.RoutineExerciseDto
import com.gymbaswara.app.core.network.SyncRequest
import com.gymbaswara.app.core.network.WorkoutDto
import com.gymbaswara.app.core.network.WorkoutExerciseDto
import com.gymbaswara.app.core.network.WorkoutSetDto
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncEngine @Inject constructor(
    private val api: GymBaswaraApi,
    private val workoutDao: WorkoutDao,
    private val routineDao: RoutineDao,
    private val bodyMetricDao: BodyMetricDao,
    private val sessionManager: SessionManager,
    private val preferencesManager: PreferencesManager
) {

    suspend fun push(): Boolean {
        val pendingWorkouts = workoutDao.getPendingWorkouts()
        val pendingRoutines = routineDao.getPendingRoutineEntities()
        val routineCrossRefs = if (pendingRoutines.isEmpty()) {
            emptyList()
        } else {
            routineDao.getCrossRefsForRoutines(pendingRoutines.map { it.id })
        }
        val pendingMetrics = bodyMetricDao.getPending()
        val deletedRoutineIds = routineDao.getPendingDeleteIds()
        val deletedMetricIds = bodyMetricDao.getPendingDeleteIds()

        val hasWork = pendingWorkouts.isNotEmpty() || pendingRoutines.isNotEmpty() ||
            pendingMetrics.isNotEmpty() || deletedRoutineIds.isNotEmpty() ||
            deletedMetricIds.isNotEmpty()
        if (!hasWork) return true

        val request = SyncRequest(
            workouts = pendingWorkouts.map { detail ->
                WorkoutDto(
                    id = detail.workout.id,
                    name = detail.workout.name,
                    startedAt = detail.workout.startedAt,
                    completedAt = detail.workout.completedAt,
                    durationSeconds = detail.workout.durationSeconds,
                    totalVolume = detail.workout.totalVolume,
                    status = detail.workout.status,
                    exercises = detail.exercises.map { exerciseWithSets ->
                        WorkoutExerciseDto(
                            id = exerciseWithSets.exercise.id,
                            exerciseId = exerciseWithSets.exercise.exerciseId,
                            notes = exerciseWithSets.exercise.notes,
                            sets = exerciseWithSets.sets.map { set ->
                                WorkoutSetDto(
                                    id = set.id,
                                    setNumber = set.setNumber,
                                    weight = set.weight,
                                    reps = set.reps,
                                    isCompleted = set.isCompleted
                                )
                            }
                        )
                    }
                )
            },
            routines = pendingRoutines.map { routine ->
                RoutineDto(
                    id = routine.id,
                    name = routine.name,
                    description = routine.description,
                    exercises = routineCrossRefs
                        .filter { it.routineId == routine.id }
                        .sortedBy { it.orderIndex }
                        .map { RoutineExerciseDto(it.id, it.exerciseId, it.orderIndex, it.notes) },
                    createdAt = routine.createdAt,
                    updatedAt = routine.updatedAt
                )
            },
            bodyMetrics = pendingMetrics.map { it.toDto() },
            deletedRoutines = deletedRoutineIds,
            deletedBodyMetrics = deletedMetricIds
        )

        val response = api.pushData(request)
        if (!(response.isSuccessful && response.body()?.data?.success == true)) return false

        if (pendingWorkouts.isNotEmpty()) workoutDao.updateSyncStatus(pendingWorkouts.map { it.workout.id })
        if (pendingRoutines.isNotEmpty()) routineDao.markSynced(pendingRoutines.map { it.id })
        if (pendingMetrics.isNotEmpty()) bodyMetricDao.markSynced(pendingMetrics.map { it.id })
        if (deletedRoutineIds.isNotEmpty()) routineDao.hardDeleteByIds(deletedRoutineIds)
        if (deletedMetricIds.isNotEmpty()) bodyMetricDao.hardDeleteByIds(deletedMetricIds)
        return true
    }

    suspend fun pull(): Boolean {
        val since = preferencesManager.preferences.first().lastSyncAt
        val response = api.pullData(since)
        val data = response.body()?.data
        if (!response.isSuccessful || data == null) return false

        val userId = sessionManager.session.first().userId ?: "server"
        data.workouts.orEmpty().forEach { storeWorkout(it, userId) }

        // Skip records still carrying local edits so a pull never clobbers them;
        // they will be pushed first and marked SYNCED on the next cycle.
        data.routines.orEmpty()
            .filter { routineDao.getSyncStatus(it.id) != "PENDING" }
            .forEach { storeRoutine(it) }

        val metricDtos = data.bodyMetrics.orEmpty()
            .filter { bodyMetricDao.getSyncStatus(it.id) != "PENDING" }
        if (metricDtos.isNotEmpty()) {
            bodyMetricDao.upsertAll(metricDtos.map { it.toEntity() })
        }

        preferencesManager.setLastSyncAt(data.serverTime)
        return true
    }

    private suspend fun storeWorkout(dto: WorkoutDto, userId: String) {
        val now = System.currentTimeMillis()
        val workout = WorkoutEntity(
            id = dto.id,
            userId = userId,
            routineId = null,
            routineDayId = null,
            name = dto.name,
            startedAt = dto.startedAt,
            completedAt = dto.completedAt,
            durationSeconds = dto.durationSeconds,
            totalVolume = dto.totalVolume,
            status = dto.status,
            syncStatus = "SYNCED",
            createdAt = dto.startedAt,
            updatedAt = now
        )

        val exercises = dto.exercises.mapIndexed { index, exercise ->
            WorkoutExerciseEntity(
                id = exercise.id,
                workoutId = dto.id,
                exerciseId = exercise.exerciseId,
                orderIndex = index,
                supersetId = null,
                notes = exercise.notes
            )
        }

        val sets = dto.exercises.flatMap { exercise ->
            exercise.sets.map { set ->
                WorkoutSetEntity(
                    id = set.id,
                    workoutExerciseId = exercise.id,
                    setNumber = set.setNumber,
                    setType = "normal",
                    weight = set.weight,
                    reps = set.reps,
                    durationSeconds = null,
                    distance = null,
                    rpe = null,
                    rir = null,
                    isCompleted = set.isCompleted,
                    completedAt = null
                )
            }
        }

        workoutDao.insertWorkoutWithDetails(workout, exercises, sets)
    }

    private suspend fun storeRoutine(dto: RoutineDto) {
        val routine = RoutineEntity(
            id = dto.id,
            name = dto.name,
            description = dto.description,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt,
            syncStatus = "SYNCED"
        )
        val crossRefs = dto.exercises.sortedBy { it.orderIndex }.mapIndexed { index, exercise ->
            RoutineExerciseCrossRef(
                id = exercise.id,
                routineId = dto.id,
                exerciseId = exercise.exerciseId,
                orderIndex = index,
                notes = exercise.notes
            )
        }
        routineDao.insertRoutineWithExercises(routine, crossRefs)
    }

    private fun BodyMetricEntity.toDto() = BodyMetricDto(
        id = id,
        weightKg = weightKg,
        bodyFatPercent = bodyFatPercent,
        chestCm = chestCm,
        waistCm = waistCm,
        armCm = armCm,
        thighCm = thighCm,
        notes = notes,
        recordedAt = recordedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun BodyMetricDto.toEntity() = BodyMetricEntity(
        id = id,
        weightKg = weightKg,
        bodyFatPercent = bodyFatPercent,
        chestCm = chestCm,
        waistCm = waistCm,
        armCm = armCm,
        thighCm = thighCm,
        notes = notes,
        recordedAt = recordedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = "SYNCED"
    )
}
