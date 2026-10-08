package com.gymbaswara.app.data.repository

import com.gymbaswara.app.core.database.dao.RoutineDao
import com.gymbaswara.app.core.database.entity.RoutineEntity
import com.gymbaswara.app.core.database.entity.RoutineExerciseCrossRef
import com.gymbaswara.app.data.mapper.toDomain
import com.gymbaswara.app.domain.model.Routine
import com.gymbaswara.app.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoutineRepositoryImpl @Inject constructor(
    private val routineDao: RoutineDao
) : RoutineRepository {

    override fun getAllRoutines(): Flow<List<Routine>> {
        return routineDao.getAllRoutines().combine(routineDao.getAllCrossRefs()) { routinesWithExercises, crossRefs ->
            val orderMap = crossRefs.groupBy { it.routineId }
                .mapValues { entry -> entry.value.associate { it.exerciseId to it.orderIndex } }

            routinesWithExercises.map { routineWithEx ->
                val routineOrders = orderMap[routineWithEx.routine.id] ?: emptyMap()
                val sortedExercises = routineWithEx.exercises.sortedBy { routineOrders[it.id] ?: Int.MAX_VALUE }
                Routine(
                    id = routineWithEx.routine.id,
                    name = routineWithEx.routine.name,
                    description = routineWithEx.routine.description,
                    createdAt = routineWithEx.routine.createdAt,
                    updatedAt = routineWithEx.routine.updatedAt,
                    exercises = sortedExercises.map { it.toDomain() }
                )
            }
        }
    }

    override fun getRoutineById(id: String): Flow<Routine?> {
        return routineDao.getRoutineById(id).combine(routineDao.getCrossRefsByRoutineId(id)) { routineWithEx, crossRefs ->
            routineWithEx?.let {
                val orderMap = crossRefs.associate { it.exerciseId to it.orderIndex }
                val sortedExercises = it.exercises.sortedBy { ex -> orderMap[ex.id] ?: Int.MAX_VALUE }
                Routine(
                    id = it.routine.id,
                    name = it.routine.name,
                    description = it.routine.description,
                    createdAt = it.routine.createdAt,
                    updatedAt = it.routine.updatedAt,
                    exercises = sortedExercises.map { ex -> ex.toDomain() }
                )
            }
        }
    }

    override suspend fun saveRoutine(routine: Routine) {
        val routineEntity = RoutineEntity(
            id = routine.id,
            name = routine.name,
            description = routine.description,
            createdAt = routine.createdAt,
            updatedAt = routine.updatedAt
        )
        val crossRefs = routine.exercises.mapIndexed { index, exercise ->
            RoutineExerciseCrossRef(
                routineId = routine.id,
                exerciseId = exercise.id,
                orderIndex = index
            )
        }
        routineDao.insertRoutineWithExercises(routineEntity, crossRefs)
    }

    override suspend fun deleteRoutine(id: String) {
        routineDao.deleteRoutine(id)
    }
}
