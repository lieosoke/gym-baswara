package com.gymbaswara.app.feature.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymbaswara.app.domain.model.Routine
import com.gymbaswara.app.domain.repository.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutineListViewModel @Inject constructor(
    private val routineRepository: RoutineRepository
) : ViewModel() {

    val routines: StateFlow<List<Routine>> = routineRepository.getAllRoutines()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun deleteRoutine(id: String) {
        viewModelScope.launch { routineRepository.deleteRoutine(id) }
    }
}
