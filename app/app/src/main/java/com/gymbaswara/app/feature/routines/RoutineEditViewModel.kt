package com.gymbaswara.app.feature.routines

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymbaswara.app.domain.model.Exercise
import com.gymbaswara.app.domain.model.Routine
import com.gymbaswara.app.domain.model.RoutineExercise
import com.gymbaswara.app.domain.repository.ExerciseRepository
import com.gymbaswara.app.domain.repository.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class EditableRoutineExercise(
    val instanceId: String,
    val exercise: Exercise,
    val notes: String? = null
)

data class RoutineEditUiState(
    val name: String = "",
    val description: String = "",
    val selected: List<EditableRoutineExercise> = emptyList(),
    val available: List<Exercise> = emptyList(),
    val isEditing: Boolean = false,
    val saved: Boolean = false,
    val createdAt: Long = 0L
)

@HiltViewModel
class RoutineEditViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val exerciseRepository: ExerciseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val routineId: String? = savedStateHandle.get<String>("routineId")
        ?.takeIf { it.isNotBlank() && it != "new" }

    private val _state = MutableStateFlow(RoutineEditUiState())
    val state: StateFlow<RoutineEditUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            exerciseRepository.getExercises().collect { exercises ->
                _state.update { it.copy(available = exercises) }
            }
        }

        if (routineId != null) {
            viewModelScope.launch {
                val routine = routineRepository.getRoutineById(routineId).firstOrNull()
                if (routine != null) {
                    _state.update {
                        it.copy(
                            name = routine.name,
                            description = routine.description.orEmpty(),
                            selected = routine.exercises.map { re ->
                                EditableRoutineExercise(re.instanceId, re.exercise, re.notes)
                            },
                            isEditing = true,
                            createdAt = routine.createdAt
                        )
                    }
                }
            }
        }
    }

    fun updateName(name: String) = _state.update { it.copy(name = name) }

    fun updateDescription(description: String) = _state.update { it.copy(description = description) }

    fun addExercise(exercise: Exercise) {
        _state.update { current ->
            if (current.selected.any { it.exercise.id == exercise.id }) {
                current
            } else {
                current.copy(
                    selected = current.selected + EditableRoutineExercise(
                        instanceId = UUID.randomUUID().toString(),
                        exercise = exercise
                    )
                )
            }
        }
    }

    fun removeExercise(instanceId: String) {
        _state.update { it.copy(selected = it.selected.filterNot { ex -> ex.instanceId == instanceId }) }
    }

    fun moveExercise(instanceId: String, delta: Int) {
        _state.update { current ->
            val list = current.selected.toMutableList()
            val index = list.indexOfFirst { it.instanceId == instanceId }
            val target = index + delta
            if (index < 0 || target < 0 || target >= list.size) {
                current
            } else {
                val item = list.removeAt(index)
                list.add(target, item)
                current.copy(selected = list)
            }
        }
    }

    fun updateNotes(instanceId: String, notes: String) {
        _state.update { current ->
            current.copy(
                selected = current.selected.map {
                    if (it.instanceId == instanceId) it.copy(notes = notes.ifBlank { null }) else it
                }
            )
        }
    }

    fun save() {
        val current = _state.value
        if (current.name.isBlank()) return

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val routine = Routine(
                id = routineId ?: UUID.randomUUID().toString(),
                name = current.name.trim(),
                description = current.description.trim().ifBlank { null },
                exercises = current.selected.map {
                    RoutineExercise(instanceId = it.instanceId, exercise = it.exercise, notes = it.notes)
                },
                createdAt = if (current.createdAt > 0) current.createdAt else now,
                updatedAt = now
            )
            routineRepository.saveRoutine(routine)
            _state.update { it.copy(saved = true) }
        }
    }
}
