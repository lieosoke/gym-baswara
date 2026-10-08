package com.gymbaswara.app.domain.usecase

import com.gymbaswara.app.domain.model.Exercise
import com.gymbaswara.app.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetExercisesUseCase @Inject constructor(
    private val repository: ExerciseRepository
) {
    operator fun invoke(): Flow<List<Exercise>> {
        return repository.getExercises()
    }
}
