package com.gymbaswara.app.core.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gymbaswara.app.core.database.dao.WorkoutDao
import com.gymbaswara.app.core.network.GymBaswaraApi
import com.gymbaswara.app.core.network.SyncRequest
import com.gymbaswara.app.core.network.WorkoutDto
import com.gymbaswara.app.core.network.WorkoutExerciseDto
import com.gymbaswara.app.core.network.WorkoutSetDto
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val workoutDao: WorkoutDao,
    private val api: GymBaswaraApi
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val pendingWorkouts = workoutDao.getPendingWorkouts()
            
            if (pendingWorkouts.isEmpty()) {
                return Result.success()
            }
            
            val workoutDtos = pendingWorkouts.map { detail ->
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
            }
            
            val request = SyncRequest(workouts = workoutDtos)
            val response = api.pushData(request)
            
            if (response.isSuccessful && response.body()?.data?.success == true) {
                // Update room database to SYNCED
                val ids = pendingWorkouts.map { it.workout.id }
                workoutDao.updateSyncStatus(ids)
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
