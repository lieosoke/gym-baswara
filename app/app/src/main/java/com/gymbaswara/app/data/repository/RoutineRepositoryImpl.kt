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
            val crossRefsByRoutine = crossRefs.groupBy { it.routineId }

            routinesWithExercises.map { routineWithEx ->
                val routineRefs = crossRefsByRoutine[routineWithEx.routine.id] ?: emptyList()
                val sortedRefs = routineRefs.sortedBy { it.orderIndex }
                val exercises = sortedRefs.mapNotNull { ref ->
                    val exEntity = routineWithEx.exercises.find { it.id == ref.exerciseId }
                    if (exEntity != null) {
                        com.gymbaswara.app.domain.model.RoutineExercise(
                            instanceId = ref.id,
                            exercise = exEntity.toDomain(),
                            notes = ref.notes
                        )
                    } else null
                }
                Routine(
                    id = routineWithEx.routine.id,
                    name = routineWithEx.routine.name,
                    description = routineWithEx.routine.description,
                    createdAt = routineWithEx.routine.createdAt,
                    updatedAt = routineWithEx.routine.updatedAt,
                    exercises = exercises
                )
            }
        }
    }

    override fun getRoutineById(id: String): Flow<Routine?> {
        return routineDao.getRoutineById(id).combine(routineDao.getCrossRefsByRoutineId(id)) { routineWithEx, crossRefs ->
            routineWithEx?.let {
                val sortedRefs = crossRefs.sortedBy { it.orderIndex }
                val exercises = sortedRefs.mapNotNull { ref ->
                    val exEntity = it.exercises.find { entity -> entity.id == ref.exerciseId }
                    if (exEntity != null) {
                        com.gymbaswara.app.domain.model.RoutineExercise(
                            instanceId = ref.id,
                            exercise = exEntity.toDomain(),
                            notes = ref.notes
                        )
                    } else null
                }
                Routine(
                    id = it.routine.id,
                    name = it.routine.name,
                    description = it.routine.description,
                    createdAt = it.routine.createdAt,
                    updatedAt = it.routine.updatedAt,
                    exercises = exercises
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
        val crossRefs = routine.exercises.mapIndexed { index, routineEx ->
            RoutineExerciseCrossRef(
                id = routineEx.instanceId,
                routineId = routine.id,
                exerciseId = routineEx.exercise.id,
                orderIndex = index,
                notes = routineEx.notes
            )
        }
        routineDao.insertRoutineWithExercises(routineEntity, crossRefs)
    }

    override suspend fun deleteRoutine(id: String) {
        routineDao.deleteRoutine(id)
    }
}
