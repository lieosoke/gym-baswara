package com.gymbaswara.app.data.repository

import com.gymbaswara.app.core.database.dao.ExerciseDao
import com.gymbaswara.app.data.mapper.toDomain
import com.gymbaswara.app.domain.model.Exercise
import com.gymbaswara.app.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao
) : ExerciseRepository {
    override fun getExercises(): Flow<List<Exercise>> {
        return exerciseDao.getAllExercises().map { it.toDomain() }
    }
}
