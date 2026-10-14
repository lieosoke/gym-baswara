package com.gymbaswara.app.feature.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymbaswara.app.domain.model.Exercise
import com.gymbaswara.app.domain.model.PRProgression
import com.gymbaswara.app.domain.repository.ExerciseRepository
import com.gymbaswara.app.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProgressUiState(
    val exercises: List<Exercise> = emptyList(),
    val selectedExerciseId: String? = null,
    val prHistory: List<PRProgression> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _selectedExerciseId = MutableStateFlow<String?>(null)

    val exercises: StateFlow<List<Exercise>> = kotlinx.coroutines.flow.combine(
        exerciseRepository.getExercises(),
        workoutRepository.getExercisedIds()
    ) { allExercises, exercisedIds ->
        allExercises.filter { it.id in exercisedIds }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val prHistory: StateFlow<List<PRProgression>> = _selectedExerciseId
        .flatMapLatest { exerciseId ->
            if (exerciseId == null) {
                MutableStateFlow(emptyList())
            } else {
                workoutRepository.getPRProgression(exerciseId)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val selectedExerciseId: StateFlow<String?> = _selectedExerciseId.asStateFlow()

    fun selectExercise(exerciseId: String) {
        _selectedExerciseId.value = exerciseId
    }
    
    init {
        // Auto select first exercise when loaded
        viewModelScope.launch {
            exerciseRepository.getExercises().collect { list ->
                if (list.isNotEmpty() && _selectedExerciseId.value == null) {
                    _selectedExerciseId.value = list.first().id
                }
            }
        }
    }
}
