package com.gymbaswara.app.feature.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymbaswara.app.domain.model.Exercise
import com.gymbaswara.app.domain.usecase.GetExercisesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ExerciseListViewModel @Inject constructor(
    private val getExercisesUseCase: GetExercisesUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedMuscle = MutableStateFlow("Semua Otot")
    val selectedMuscle = _selectedMuscle.asStateFlow()

    private val _selectedEquipment = MutableStateFlow("Semua Alat")
    val selectedEquipment = _selectedEquipment.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)

    private val _exercisesFlow = getExercisesUseCase()
        .onStart { /* handle start */ }
        .catch { e -> _error.value = e.message ?: "Unknown error" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filterOptions: StateFlow<Pair<List<String>, List<String>>> = _exercisesFlow.map { exercises ->
        val muscles = listOf("Semua Otot") + exercises.mapNotNull { it.primaryMuscle }.distinct().sorted()
        val equipments = listOf("Semua Alat") + exercises.mapNotNull { it.equipment }.distinct().sorted()
        Pair(muscles, equipments)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(listOf("Semua Otot"), listOf("Semua Alat")))

    val uiState: StateFlow<ExerciseListUiState> = combine(
        _exercisesFlow,
        _searchQuery,
        _selectedMuscle,
        _selectedEquipment,
        _error
    ) { exercises, query, muscle, equipment, errorMsg ->
        if (errorMsg != null) {
            return@combine ExerciseListUiState.Error(errorMsg)
        }
        
        if (exercises.isEmpty() && query.isEmpty() && muscle == "Semua Otot" && equipment == "Semua Alat") {
            // Assume loading if database is empty initially, though in real app we'd have a better DB state check
            // For simplicity we just return Success with empty map if it's really empty
        }

        val filtered = exercises.filter {
            val matchesSearch = it.name.contains(query, ignoreCase = true)
            val matchesMuscle = muscle == "Semua Otot" || it.primaryMuscle.equals(muscle, ignoreCase = true)
            val matchesEquipment = equipment == "Semua Alat" || it.equipment.equals(equipment, ignoreCase = true)
            matchesSearch && matchesMuscle && matchesEquipment
        }

        val grouped = filtered.groupBy { it.primaryMuscle ?: "Lainnya" }
        ExerciseListUiState.Success(grouped)
    }.flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExerciseListUiState.Loading
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onMuscleSelected(muscle: String) {
        _selectedMuscle.value = muscle
    }

    fun onEquipmentSelected(equipment: String) {
        _selectedEquipment.value = equipment
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }
}

sealed interface ExerciseListUiState {
    object Loading : ExerciseListUiState
    data class Success(val groupedExercises: Map<String, List<Exercise>>) : ExerciseListUiState
    data class Error(val message: String) : ExerciseListUiState
}
