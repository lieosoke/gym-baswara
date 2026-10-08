package com.gymbaswara.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sets")
data class WorkoutSetEntity(
    @PrimaryKey val id: String,
    val workoutExerciseId: String,
    val setNumber: Int,
    val setType: String,
    val weight: Double,
    val reps: Int,
    val durationSeconds: Int?,
    val distance: Double?,
    val rpe: Double?,
    val rir: Int?,
    val isCompleted: Boolean,
    val completedAt: Long?
)
