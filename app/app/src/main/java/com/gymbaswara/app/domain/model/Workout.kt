package com.gymbaswara.app.domain.model

data class Workout(
    val id: String,
    val routineId: String? = null,
    val name: String,
    val durationSeconds: Int,
    val exercises: List<WorkoutExercise>
)

data class WorkoutExercise(
    val id: String,
    val exerciseId: String,
    val exerciseName: String,
    val sets: List<WorkoutSet>
)

data class WorkoutSet(
    val id: String,
    val setNumber: Int,
    val weight: Double,
    val reps: Int,
    val isCompleted: Boolean
)
