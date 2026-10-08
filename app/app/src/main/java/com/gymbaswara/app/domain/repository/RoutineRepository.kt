package com.gymbaswara.app.domain.repository

import com.gymbaswara.app.domain.model.Routine
import kotlinx.coroutines.flow.Flow

interface RoutineRepository {
    fun getAllRoutines(): Flow<List<Routine>>
    fun getRoutineById(id: String): Flow<Routine?>
    suspend fun saveRoutine(routine: Routine)
    suspend fun deleteRoutine(id: String)
}
