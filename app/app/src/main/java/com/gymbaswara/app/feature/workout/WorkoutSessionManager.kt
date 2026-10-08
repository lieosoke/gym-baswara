package com.gymbaswara.app.feature.workout

import android.os.SystemClock
import com.gymbaswara.app.domain.model.Exercise
import com.gymbaswara.app.domain.model.Routine
import com.gymbaswara.app.domain.model.Workout
import com.gymbaswara.app.domain.model.WorkoutExercise
import com.gymbaswara.app.domain.model.WorkoutSet
import com.gymbaswara.app.domain.repository.ExerciseRepository
import com.gymbaswara.app.domain.repository.RoutineRepository
import com.gymbaswara.app.domain.repository.WorkoutRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutSessionManager @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val routineRepository: RoutineRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _uiState = MutableStateFlow(ActiveWorkoutUiState())
    val uiState: StateFlow<ActiveWorkoutUiState> = _uiState.asStateFlow()

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()
    
    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private var timerJob: Job? = null
    private var lastTickTime: Long = 0

    fun startWorkout(routineId: String?, routineName: String?, exercises: List<WorkoutExerciseState> = emptyList()) {
        if (_isActive.value) return
        
        _isActive.value = true
        _isPaused.value = false
        
        _uiState.value = ActiveWorkoutUiState(
            workoutName = routineName ?: "Latihan Baru",
            routineId = routineId,
            exercises = exercises,
            workoutDurationSeconds = 0,
            restTimerSeconds = 0,
            isRestTimerActive = false
        )
        
        startTimer()
        loadHistoryForExercises()
    }

    private fun loadHistoryForExercises() {
        scope.launch {
            val currentExercises = _uiState.value.exercises
            val updatedExercises = currentExercises.map { ex ->
                val historyText = workoutRepository.getLastPerformance(ex.exerciseId) ?: "Belum ada riwayat"
                val historySets = workoutRepository.getLastPerformanceSets(ex.exerciseId)
                
                val setsToUse = if (historySets.isNotEmpty() && ex.sets.size == 1 && ex.sets[0].weight.isEmpty() && ex.sets[0].reps.isEmpty()) {
                    historySets.map { hs ->
                        val weightStr = if (hs.weight % 1.0 == 0.0) hs.weight.toInt().toString() else hs.weight.toString()
                        WorkoutSetState(
                            setNumber = hs.setNumber,
                            weight = if (hs.weight > 0) weightStr else "",
                            reps = if (hs.reps > 0) hs.reps.toString() else "",
                            isCompleted = false
                        )
                    }
                } else {
                    ex.sets
                }

                ex.copy(lastPerformance = historyText, sets = setsToUse)
            }
            _uiState.update { it.copy(exercises = updatedExercises) }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        lastTickTime = SystemClock.elapsedRealtime()
        timerJob = scope.launch {
            while (true) {
                delay(1000)
                if (_isPaused.value) {
                    lastTickTime = SystemClock.elapsedRealtime() // prevent jumping when resumed
                    continue
                }
                
                val currentTickTime = SystemClock.elapsedRealtime()
                val deltaMillis = currentTickTime - lastTickTime
                val deltaSeconds = (deltaMillis / 1000).toInt()
                
                if (deltaSeconds > 0) {
                    lastTickTime += deltaSeconds * 1000L
                    
                    _uiState.update { state ->
                        val newWorkoutDuration = state.workoutDurationSeconds + deltaSeconds
                        var newRestTimer = state.restTimerSeconds
                        var restActive = state.isRestTimerActive
    
                        if (restActive) {
                            newRestTimer -= deltaSeconds
                            if (newRestTimer <= 0) {
                                restActive = false
                                newRestTimer = 0
                            }
                        }
    
                        state.copy(
                            workoutDurationSeconds = newWorkoutDuration,
                            restTimerSeconds = newRestTimer,
                            isRestTimerActive = restActive
                        )
                    }
                }
            }
        }
    }

    fun pauseTimer() {
        _isPaused.value = true
    }

    fun resumeTimer() {
        _isPaused.value = false
        lastTickTime = SystemClock.elapsedRealtime()
    }

    fun updateWorkoutName(name: String) {
        _uiState.update { it.copy(workoutName = name) }
    }

    fun addExercise(id: String, name: String) {
        _uiState.update { state ->
            val newExercise = WorkoutExerciseState(exerciseId = id, exerciseName = name)
            state.copy(exercises = state.exercises + newExercise)
        }
        loadHistoryForExercises()
    }

    fun addSet(exerciseId: String) {
        _uiState.update { state ->
            state.copy(
                exercises = state.exercises.map { exercise ->
                    if (exercise.id == exerciseId) {
                        val newSetNumber = exercise.sets.size + 1
                        exercise.copy(sets = exercise.sets + WorkoutSetState(setNumber = newSetNumber))
                    } else {
                        exercise
                    }
                }
            )
        }
    }

    fun removeSet(exerciseId: String, setId: String) {
        _uiState.update { state ->
            state.copy(
                exercises = state.exercises.map { exercise ->
                    if (exercise.id == exerciseId) {
                        val newSets = exercise.sets.filterNot { it.id == setId }
                            .mapIndexed { index, set -> set.copy(setNumber = index + 1) }
                        exercise.copy(sets = newSets)
                    } else {
                        exercise
                    }
                }
            )
        }
    }

    fun updateSet(exerciseId: String, setId: String, weight: String? = null, reps: String? = null, isCompleted: Boolean? = null) {
        _uiState.update { state ->
            var shouldStartRest = false
            val newExercises = state.exercises.map { exercise ->
                if (exercise.id == exerciseId) {
                    exercise.copy(
                        sets = exercise.sets.map { set ->
                            if (set.id == setId) {
                                val wasCompleted = set.isCompleted
                                val nowCompleted = isCompleted ?: set.isCompleted
                                
                                if (!wasCompleted && nowCompleted) {
                                    shouldStartRest = true
                                }
                                
                                set.copy(
                                    weight = weight ?: set.weight,
                                    reps = reps ?: set.reps,
                                    isCompleted = nowCompleted
                                )
                            } else {
                                set
                            }
                        }
                    )
                } else {
                    exercise
                }
            }

            state.copy(
                exercises = newExercises,
                isRestTimerActive = if (shouldStartRest) true else state.isRestTimerActive,
                restTimerSeconds = if (shouldStartRest) 90 else state.restTimerSeconds
            )
        }
    }

    fun skipRestTimer() {
        _uiState.update { it.copy(isRestTimerActive = false, restTimerSeconds = 0) }
    }

    fun adjustRestTimer(addSeconds: Int) {
        _uiState.update { state ->
            val newTime = (state.restTimerSeconds + addSeconds).coerceAtLeast(0)
            state.copy(
                restTimerSeconds = newTime,
                isRestTimerActive = newTime > 0
            )
        }
    }

    fun finishWorkout(onComplete: () -> Unit) {
        timerJob?.cancel()
        timerJob = null
        scope.launch {
            val state = _uiState.value
            val workout = Workout(
                id = UUID.randomUUID().toString(),
                routineId = state.routineId,
                name = state.workoutName,
                durationSeconds = state.workoutDurationSeconds,
                exercises = state.exercises.map { ex ->
                    WorkoutExercise(
                        id = ex.id,
                        exerciseId = ex.exerciseId,
                        exerciseName = ex.exerciseName,
                        sets = ex.sets.map { set ->
                            WorkoutSet(
                                id = set.id,
                                setNumber = set.setNumber,
                                weight = set.weight.toDoubleOrNull() ?: 0.0,
                                reps = set.reps.toIntOrNull() ?: 0,
                                isCompleted = set.isCompleted
                            )
                        }
                    )
                }
            )
            workoutRepository.saveWorkout(workout)
            
            _isActive.value = false
            _isPaused.value = false
            _uiState.value = ActiveWorkoutUiState()
            
            onComplete()
        }
    }
}
