package com.gymbaswara.app.data.repository

import kotlinx.coroutines.flow.map

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.gymbaswara.app.core.database.dao.WorkoutDao
import com.gymbaswara.app.core.database.entity.WorkoutEntity
import com.gymbaswara.app.core.database.entity.WorkoutExerciseEntity
import com.gymbaswara.app.core.database.entity.WorkoutSetEntity
import com.gymbaswara.app.core.sync.SyncWorker
import com.gymbaswara.app.domain.model.Workout
import com.gymbaswara.app.domain.model.WorkoutSet
import com.gymbaswara.app.domain.repository.WorkoutRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WorkoutRepositoryImpl @Inject constructor(
    private val workoutDao: WorkoutDao,
    @ApplicationContext private val context: Context
) : WorkoutRepository {

    override suspend fun saveWorkout(workout: Workout) {
        val workoutEntity = WorkoutEntity(
            id = workout.id,
            userId = "user_1",
            routineId = workout.routineId,
            routineDayId = null,
            name = workout.name,
            startedAt = System.currentTimeMillis() - (workout.durationSeconds * 1000L),
            completedAt = System.currentTimeMillis(),
            durationSeconds = workout.durationSeconds,
            totalVolume = workout.exercises.flatMap { it.sets }.filter { it.isCompleted }.sumOf { it.weight * it.reps },
            status = "completed",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val exerciseEntities = workout.exercises.mapIndexed { index, ex ->
            WorkoutExerciseEntity(
                id = ex.id,
                workoutId = workout.id,
                exerciseId = ex.exerciseId,
                orderIndex = index,
                supersetId = null
            )
        }

        val setEntities = workout.exercises.flatMap { ex ->
            ex.sets.map { set ->
                WorkoutSetEntity(
                    id = set.id,
                    workoutExerciseId = ex.id,
                    setNumber = set.setNumber,
                    setType = "normal",
                    weight = set.weight,
                    reps = set.reps,
                    durationSeconds = null,
                    distance = null,
                    rpe = null,
                    rir = null,
                    isCompleted = set.isCompleted,
                    completedAt = if (set.isCompleted) System.currentTimeMillis() else null
                )
            }
        }

        workoutDao.insertWorkoutWithDetails(workoutEntity, exerciseEntities, setEntities)

        // Trigger background sync
        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>().build()
        WorkManager.getInstance(context).enqueue(syncWorkRequest)
    }

    override suspend fun getLastPerformance(exerciseId: String): String? {
        val lastSet = workoutDao.getLastPerformance(exerciseId) ?: return null
        return "Terakhir: ${lastSet.weight} kg x ${lastSet.reps} reps"
    }

    override suspend fun getLastPerformanceSets(exerciseId: String): List<WorkoutSet> {
        return workoutDao.getLastPerformanceSets(exerciseId).map {
            WorkoutSet(
                id = it.id,
                setNumber = it.setNumber,
                weight = it.weight,
                reps = it.reps,
                isCompleted = it.isCompleted
            )
        }
    }

    override fun getStatsSince(startTime: Long): kotlinx.coroutines.flow.Flow<com.gymbaswara.app.domain.model.ActivityStats> {
        return workoutDao.getStatsSince(startTime).map { daoStats ->
            com.gymbaswara.app.domain.model.ActivityStats(
                sessionCount = daoStats.sessionCount,
                totalVolume = daoStats.totalVolume
            )
        }
    }
}
