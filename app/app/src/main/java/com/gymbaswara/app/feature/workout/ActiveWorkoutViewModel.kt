package com.gymbaswara.app.feature.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymbaswara.app.domain.repository.ExerciseRepository
import com.gymbaswara.app.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.firstOrNull
import com.gymbaswara.app.domain.model.Workout
import com.gymbaswara.app.domain.model.WorkoutExercise
import com.gymbaswara.app.domain.model.WorkoutSet
import com.gymbaswara.app.domain.repository.WorkoutRepository
import android.os.SystemClock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class WorkoutSetState(
    val id: String = UUID.randomUUID().toString(),
    val setNumber: Int,
    val weight: String = "",
    val reps: String = "",
    val isCompleted: Boolean = false
)

data class WorkoutExerciseState(
    val id: String = UUID.randomUUID().toString(),
    val exerciseId: String,
    val exerciseName: String,
    val lastPerformance: String = "Belum ada riwayat",
    val sets: List<WorkoutSetState> = listOf(WorkoutSetState(setNumber = 1))
)

data class ActiveWorkoutDataState(
    val workoutName: String = "Latihan Baru",
    val routineId: String? = null,
    val exercises: List<WorkoutExerciseState> = emptyList()
)

data class ActiveWorkoutTimerState(
    val workoutDurationSeconds: Int = 0,
    val isRestTimerActive: Boolean = false,
    val restTimerSeconds: Int = 0
) {
    val formattedWorkoutTimer: String
        get() {
            val h = workoutDurationSeconds / 3600
            val m = (workoutDurationSeconds % 3600) / 60
            val s = workoutDurationSeconds % 60
            return if (h > 0) String.format("%02d:%02d:%02d", h, m, s)
            else String.format("%02d:%02d", m, s)
        }

    val formattedRestTimer: String
        get() {
            val m = restTimerSeconds / 60
            val s = restTimerSeconds % 60
            return String.format("%02d:%02d", m, s)
        }
}

@HiltViewModel
class ActiveWorkoutViewModel @Inject constructor(
    private val sessionManager: WorkoutSessionManager,
    exerciseRepository: ExerciseRepository
) : ViewModel() {

    val allExercises: StateFlow<List<com.gymbaswara.app.domain.model.Exercise>> = exerciseRepository.getExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dataState: StateFlow<ActiveWorkoutDataState> = sessionManager.dataState
    val timerState: StateFlow<ActiveWorkoutTimerState> = sessionManager.timerState
    val isPaused: StateFlow<Boolean> = sessionManager.isPaused

    fun updateWorkoutName(name: String) = sessionManager.updateWorkoutName(name)
    fun addExercise(id: String, name: String) = sessionManager.addExercise(id, name)
    fun addSet(exerciseId: String) = sessionManager.addSet(exerciseId)
    fun removeSet(exerciseId: String, setId: String) = sessionManager.removeSet(exerciseId, setId)
    fun updateSet(exerciseId: String, setId: String, weight: String? = null, reps: String? = null, isCompleted: Boolean? = null) = 
        sessionManager.updateSet(exerciseId, setId, weight, reps, isCompleted)
    fun skipRestTimer() = sessionManager.skipRestTimer()
    fun adjustRestTimer(addSeconds: Int) = sessionManager.adjustRestTimer(addSeconds)
    fun finishWorkout(onComplete: () -> Unit) = sessionManager.finishWorkout(onComplete)
    
    fun pauseTimer() = sessionManager.pauseTimer()
    fun resumeTimer() = sessionManager.resumeTimer()
}
