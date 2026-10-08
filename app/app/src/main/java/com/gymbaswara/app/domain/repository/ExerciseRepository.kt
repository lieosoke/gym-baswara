package com.gymbaswara.app.domain.repository

import com.gymbaswara.app.domain.model.Exercise
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun getExercises(): Flow<List<Exercise>>
}
