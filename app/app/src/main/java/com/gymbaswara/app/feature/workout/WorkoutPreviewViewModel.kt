package com.gymbaswara.app.feature.workout

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymbaswara.app.domain.repository.RoutineRepository
import com.gymbaswara.app.domain.model.Routine
import com.gymbaswara.app.domain.model.Exercise
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Collections
import javax.inject.Inject

@HiltViewModel
class WorkoutPreviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val routineRepository: RoutineRepository,
    private val sessionManager: WorkoutSessionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val routineId: String? = savedStateHandle["routineId"]
    private var originalRoutine: Routine? = null

    private val _uiState = MutableStateFlow(WorkoutPreviewUiState())
    val uiState: StateFlow<WorkoutPreviewUiState> = _uiState.asStateFlow()

    init {
        if (!routineId.isNullOrBlank()) {
            loadRoutine(routineId)
        }
    }

    private fun loadRoutine(id: String) {
        viewModelScope.launch {
            val routine = routineRepository.getRoutineById(id).firstOrNull()
            if (routine != null) {
                originalRoutine = routine
                val routineExercises = routine.exercises.map { exercise ->
                    WorkoutExerciseState(
                        exerciseId = exercise.id,
                        exerciseName = exercise.name,
                        lastPerformance = "Belum ada riwayat",
                        sets = listOf(WorkoutSetState(setNumber = 1))
                    )
                }
                _uiState.update { 
                    it.copy(
                        routineId = id,
                        routineName = routine.name,
                        exercises = routineExercises
                    )
                }
            }
        }
    }

    fun updateRoutineName(name: String) {
        _uiState.update { it.copy(routineName = name) }
    }

    fun addExercise(id: String, name: String) {
        _uiState.update { state ->
            val newExercise = WorkoutExerciseState(exerciseId = id, exerciseName = name)
            state.copy(exercises = state.exercises + newExercise)
        }
    }

    fun removeExercise(id: String) {
        _uiState.update { state ->
            state.copy(exercises = state.exercises.filterNot { it.id == id })
        }
    }

    fun moveExerciseUp(index: Int) {
        if (index > 0) {
            _uiState.update { state ->
                val newList = state.exercises.toMutableList()
                Collections.swap(newList, index, index - 1)
                state.copy(exercises = newList)
            }
        }
    }

    fun moveExerciseDown(index: Int) {
        if (index < _uiState.value.exercises.size - 1) {
            _uiState.update { state ->
                val newList = state.exercises.toMutableList()
                Collections.swap(newList, index, index + 1)
                state.copy(exercises = newList)
            }
        }
    }

    fun saveRoutineChanges(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val state = _uiState.value
            val original = originalRoutine ?: return@launch

            // Create placeholder Exercise objects since RoutineRepositoryImpl 
            // only requires exercise.id to recreate the cross-references.
            val updatedExercises = state.exercises.map { ex ->
                Exercise(
                    id = ex.exerciseId,
                    name = ex.exerciseName,
                    description = null,
                    equipment = null,
                    movementType = null,
                    difficulty = null,
                    primaryMuscle = null,
                    instructions = null,
                    imageUrl = null,
                    isSystem = false
                )
            }
            
            val newRoutine = original.copy(
                name = state.routineName,
                exercises = updatedExercises,
                updatedAt = System.currentTimeMillis()
            )
            
            routineRepository.saveRoutine(newRoutine)
            
            // Perbarui originalRoutine dengan yang baru
            originalRoutine = newRoutine
            onComplete()
        }
    }

    fun startWorkout(onSuccess: () -> Unit) {
        val state = _uiState.value
        sessionManager.startWorkout(
            routineId = state.routineId,
            routineName = state.routineName,
            exercises = state.exercises
        )
        
        // Start Foreground Service
        val serviceIntent = Intent(context, ActiveWorkoutService::class.java).apply {
            action = ActiveWorkoutService.ACTION_START
        }
        ContextCompat.startForegroundService(context, serviceIntent)

        onSuccess()
    }
}

data class WorkoutPreviewUiState(
    val routineId: String? = null,
    val routineName: String = "Latihan Baru",
    val exercises: List<WorkoutExerciseState> = emptyList()
)
