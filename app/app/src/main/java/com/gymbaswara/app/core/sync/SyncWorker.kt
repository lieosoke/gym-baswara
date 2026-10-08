package com.gymbaswara.app.core.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gymbaswara.app.core.database.dao.WorkoutDao
import com.gymbaswara.app.core.network.GymBaswaraApi
import com.gymbaswara.app.core.network.SyncRequest
import com.gymbaswara.app.core.network.WorkoutDto
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val workoutDao: WorkoutDao,
    private val api: GymBaswaraApi
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Get all completed workouts (simple logic for now)
            val workouts = workoutDao.getWorkouts().first()
            
            val workoutDtos = workouts.map {
                WorkoutDto(
                    id = it.id,
                    name = it.name,
                    startedAt = it.startedAt,
                    completedAt = it.completedAt,
                    durationSeconds = it.durationSeconds,
                    totalVolume = it.totalVolume,
                    status = it.status
                )
            }
            
            val request = SyncRequest(workouts = workoutDtos)
            val response = api.pushData(request)
            
            if (response.isSuccessful) {
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
