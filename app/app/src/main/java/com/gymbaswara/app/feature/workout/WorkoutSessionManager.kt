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

    private val _dataState = MutableStateFlow(ActiveWorkoutDataState())
    val dataState: StateFlow<ActiveWorkoutDataState> = _dataState.asStateFlow()
    
    private val _timerState = MutableStateFlow(ActiveWorkoutTimerState())
    val timerState: StateFlow<ActiveWorkoutTimerState> = _timerState.asStateFlow()

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()
    
    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private var timerJob: Job? = null
    private var workoutStartTime: Long = 0L
    private var totalPausedDurationMillis: Long = 0L
    private var pauseStartTime: Long = 0L
    private var targetRestEndTime: Long = 0L

    fun startWorkout(routineId: String?, routineName: String?, exercises: List<WorkoutExerciseState> = emptyList()) {
        if (_isActive.value) return
        
        _isActive.value = true
        _isPaused.value = false
        
        _dataState.value = ActiveWorkoutDataState(
            workoutName = routineName ?: "Latihan Baru",
            routineId = routineId,
            exercises = exercises
        )
        
        _timerState.value = ActiveWorkoutTimerState(
            workoutDurationSeconds = 0,
            restTimerSeconds = 0,
            isRestTimerActive = false
        )
        
        workoutStartTime = SystemClock.elapsedRealtime()
        totalPausedDurationMillis = 0L
        targetRestEndTime = 0L
        
        startTimer()
        loadHistoryForExercises()
    }

    private fun loadHistoryForExercises() {
        scope.launch {
            val currentExercises = _dataState.value.exercises
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
                            timeSeconds = if ((hs.durationSeconds ?: 0) > 0) hs.durationSeconds.toString() else "",
                            isCompleted = false
                        )
                    }
                } else {
                    ex.sets
                }

                ex.copy(lastPerformance = historyText, sets = setsToUse)
            }
            _dataState.update { it.copy(exercises = updatedExercises) }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (true) {
                delay(1000)
                if (_isPaused.value) continue
                
                val now = SystemClock.elapsedRealtime()
                val effectiveRunTime = now - workoutStartTime - totalPausedDurationMillis
                val currentDurationSeconds = (effectiveRunTime / 1000).toInt().coerceAtLeast(0)
                
                _timerState.update { state ->
                    var newRestTimer = 0
                    var restActive = state.isRestTimerActive
                    
                    if (restActive && targetRestEndTime > 0) {
                        val remainingMillis = targetRestEndTime - now
                        if (remainingMillis <= 0) {
                            restActive = false
                            newRestTimer = 0
                            targetRestEndTime = 0L
                        } else {
                            newRestTimer = (remainingMillis / 1000).toInt()
                        }
                    } else if (restActive) {
                        restActive = false
                    }
                    
                    state.copy(
                        workoutDurationSeconds = currentDurationSeconds,
                        restTimerSeconds = newRestTimer,
                        isRestTimerActive = restActive
                    )
                }
            }
        }
    }

    fun pauseTimer() {
        if (!_isPaused.value) {
            _isPaused.value = true
            pauseStartTime = SystemClock.elapsedRealtime()
        }
    }

    fun resumeTimer() {
        if (_isPaused.value) {
            _isPaused.value = false
            val pausedFor = SystemClock.elapsedRealtime() - pauseStartTime
            totalPausedDurationMillis += pausedFor
            if (targetRestEndTime > 0) {
                targetRestEndTime += pausedFor
            }
        }
    }

    fun updateWorkoutName(name: String) {
        _dataState.update { it.copy(workoutName = name) }
    }

    fun addExercise(id: String, name: String, equipment: String? = null) {
        _dataState.update { state ->
            val newExercise = WorkoutExerciseState(exerciseId = id, exerciseName = name, equipment = equipment)
            state.copy(exercises = state.exercises + newExercise)
        }
        loadHistoryForExercises()
    }

    fun addSet(exerciseId: String) {
        _dataState.update { state ->
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

    fun updateExerciseNotes(exerciseId: String, notes: String) {
        _dataState.update { state ->
            state.copy(
                exercises = state.exercises.map { exercise ->
                    if (exercise.id == exerciseId) {
                        exercise.copy(notes = notes)
                    } else {
                        exercise
                    }
                }
            )
        }
    }

    fun removeSet(exerciseId: String, setId: String) {
        _dataState.update { state ->
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

    fun updateSet(exerciseId: String, setId: String, weight: String? = null, reps: String? = null, timeSeconds: String? = null, isCompleted: Boolean? = null) {
        var shouldStartRest = false
        _dataState.update { state ->
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
                                    timeSeconds = timeSeconds ?: set.timeSeconds,
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

            state.copy(exercises = newExercises)
        }

        if (shouldStartRest) {
            targetRestEndTime = SystemClock.elapsedRealtime() + (90 * 1000L)
            _timerState.update { 
                it.copy(isRestTimerActive = true, restTimerSeconds = 90)
            }
        }
    }

    fun skipRestTimer() {
        targetRestEndTime = 0L
        _timerState.update { it.copy(isRestTimerActive = false, restTimerSeconds = 0) }
    }

    fun adjustRestTimer(addSeconds: Int) {
        _timerState.update { state ->
            val newTime = (state.restTimerSeconds + addSeconds).coerceAtLeast(0)
            targetRestEndTime += (addSeconds * 1000L)
            if (newTime <= 0) {
                targetRestEndTime = 0L
            }
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
            val data = _dataState.value
            val timer = _timerState.value
            
            // Jangan simpan latihan jika durasi kurang dari 5 menit (300 detik)
            if (timer.workoutDurationSeconds >= 300) {
                val workout = Workout(
                    id = UUID.randomUUID().toString(),
                    routineId = data.routineId,
                    name = data.workoutName,
                    durationSeconds = timer.workoutDurationSeconds,
                    exercises = data.exercises.map { ex ->
                        val isBodyweight = ex.equipment.equals("Bodyweight", ignoreCase = true)
                        val isStatic = ex.exerciseName.contains("Plank", ignoreCase = true) || ex.exerciseName.contains("Hold", ignoreCase = true)
                        
                        WorkoutExercise(
                            id = ex.id,
                            exerciseId = ex.exerciseId,
                            exerciseName = ex.exerciseName,
                            notes = ex.notes,
                            sets = ex.sets.map { set ->
                                WorkoutSet(
                                    id = set.id,
                                    setNumber = set.setNumber,
                                    weight = if (isBodyweight) 0.0 else (set.weight.toDoubleOrNull() ?: 0.0),
                                    reps = if (isStatic) 0 else (set.reps.toIntOrNull() ?: 0),
                                    durationSeconds = if (isStatic) (set.timeSeconds.toIntOrNull() ?: 0) else null,
                                    isCompleted = set.isCompleted
                                )
                            }
                        )
                    }
                )
                workoutRepository.saveWorkout(workout)
            }
            
            _isActive.value = false
            _isPaused.value = false
            _dataState.value = ActiveWorkoutDataState()
            _timerState.value = ActiveWorkoutTimerState()
            
            onComplete()
        }
    }
}
