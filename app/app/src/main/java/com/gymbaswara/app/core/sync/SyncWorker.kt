package com.gymbaswara.app.core.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncEngine: SyncEngine
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val pushed = syncEngine.push()
            val pulled = syncEngine.pull()
            if (pushed && pulled) Result.success() else Result.retry()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
