package com.gymbaswara.app.feature.workout

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.gymbaswara.app.MainActivity
import com.gymbaswara.app.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ActiveWorkoutService : Service() {

    @Inject
    lateinit var sessionManager: WorkoutSessionManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val channelId = "active_workout_channel"
    private val notificationId = 1

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_FINISH = "ACTION_FINISH"
        const val ACTION_SKIP_REST = "ACTION_SKIP_REST"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForegroundService()
                observeWorkoutState()
            }
            ACTION_PAUSE -> {
                sessionManager.pauseTimer()
            }
            ACTION_RESUME -> {
                sessionManager.resumeTimer()
            }
            ACTION_FINISH -> {
                sessionManager.finishWorkout {
                    stopSelf()
                }
            }
            ACTION_SKIP_REST -> {
                sessionManager.skipRestTimer()
            }
        }
        return START_STICKY
    }

    private fun observeWorkoutState() {
        sessionManager.timerState.onEach { timerState ->
            if (sessionManager.isActive.value) {
                updateNotification(sessionManager.dataState.value, timerState, sessionManager.isPaused.value)
            } else {
                stopSelf()
            }
        }.launchIn(serviceScope)

        sessionManager.isPaused.onEach { isPaused ->
            if (sessionManager.isActive.value) {
                updateNotification(sessionManager.dataState.value, sessionManager.timerState.value, isPaused)
            }
        }.launchIn(serviceScope)
        
        sessionManager.isActive.onEach { isActive ->
            if (!isActive) {
                stopSelf()
            }
        }.launchIn(serviceScope)
    }

    private fun startForegroundService() {
        val notification = buildNotification(sessionManager.dataState.value, sessionManager.timerState.value, sessionManager.isPaused.value)
        startForeground(notificationId, notification)
    }

    private fun updateNotification(dataState: ActiveWorkoutDataState, timerState: ActiveWorkoutTimerState, isPaused: Boolean) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, buildNotification(dataState, timerState, isPaused))
    }

    private fun buildNotification(dataState: ActiveWorkoutDataState, timerState: ActiveWorkoutTimerState, isPaused: Boolean): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseResumeActionIntent = Intent(this, ActiveWorkoutService::class.java).apply {
            action = if (isPaused) ACTION_RESUME else ACTION_PAUSE
        }
        val pauseResumePendingIntent = PendingIntent.getService(
            this,
            1,
            pauseResumeActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val finishActionIntent = Intent(this, ActiveWorkoutService::class.java).apply {
            action = ACTION_FINISH
        }
        val finishPendingIntent = PendingIntent.getService(
            this,
            2,
            finishActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val activeExercise = dataState.exercises.lastOrNull { exercise -> 
            exercise.sets.any { !it.isCompleted } 
        } ?: dataState.exercises.lastOrNull()
        
        val exerciseText = if (activeExercise != null) {
            val activeSet = activeExercise.sets.firstOrNull { !it.isCompleted } ?: activeExercise.sets.lastOrNull()
            if (activeSet != null) {
                "${activeExercise.exerciseName} - Set ${activeSet.setNumber}"
            } else {
                activeExercise.exerciseName
            }
        } else {
            "Mulai Latihan"
        }

        val contentText = if (timerState.isRestTimerActive) {
            "Istirahat: ${timerState.formattedRestTimer} | $exerciseText"
        } else {
            "Durasi: ${timerState.formattedWorkoutTimer} | $exerciseText"
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(dataState.workoutName)
            .setContentText(contentText)
            .setSmallIcon(R.mipmap.ic_launcher) // Use a proper icon in real app
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)

        if (timerState.isRestTimerActive) {
            val skipRestActionIntent = Intent(this, ActiveWorkoutService::class.java).apply {
                action = ACTION_SKIP_REST
            }
            val skipRestPendingIntent = PendingIntent.getService(
                this,
                3,
                skipRestActionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, "Skip Rest", skipRestPendingIntent)
        } else {
            val pauseResumeText = if (isPaused) "Lanjut" else "Jeda"
            builder.addAction(0, pauseResumeText, pauseResumePendingIntent)
        }

        builder.addAction(0, "Selesai", finishPendingIntent)

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Latihan Aktif",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Menampilkan durasi latihan yang sedang berjalan"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
