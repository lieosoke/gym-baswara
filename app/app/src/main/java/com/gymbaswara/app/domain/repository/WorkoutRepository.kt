package com.gymbaswara.app.domain.repository

import com.gymbaswara.app.domain.model.Workout
import com.gymbaswara.app.domain.model.WorkoutSet

interface WorkoutRepository {
    suspend fun saveWorkout(workout: Workout)
    suspend fun getLastPerformance(exerciseId: String): String?
    suspend fun getLastPerformanceSets(exerciseId: String): List<WorkoutSet>
    fun getStatsSince(startTime: Long): kotlinx.coroutines.flow.Flow<com.gymbaswara.app.domain.model.ActivityStats>
    fun getPRProgression(exerciseId: String): kotlinx.coroutines.flow.Flow<List<com.gymbaswara.app.domain.model.PRProgression>>
    fun getExercisedIds(): kotlinx.coroutines.flow.Flow<List<String>>
}
