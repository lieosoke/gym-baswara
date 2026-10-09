package com.gymbaswara.app.feature.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymbaswara.app.domain.model.Exercise
import com.gymbaswara.app.domain.model.Routine
import com.gymbaswara.app.domain.repository.ExerciseRepository
import com.gymbaswara.app.domain.repository.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class NewWorkoutViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    exerciseRepository: ExerciseRepository
) : ViewModel() {

    val routines: StateFlow<List<Routine>> = routineRepository.getAllRoutines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExercises: StateFlow<List<Exercise>> = exerciseRepository.getExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveRoutine(id: String?, name: String, description: String?, selectedExercises: List<com.gymbaswara.app.domain.model.RoutineExercise>) {
        if (name.isBlank() || selectedExercises.isEmpty()) return

        val routine = Routine(
            id = id ?: UUID.randomUUID().toString(),
            name = name,
            description = description,
            exercises = selectedExercises,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            routineRepository.saveRoutine(routine)
        }
    }

    fun deleteRoutine(id: String) {
        viewModelScope.launch {
            routineRepository.deleteRoutine(id)
        }
    }
}
