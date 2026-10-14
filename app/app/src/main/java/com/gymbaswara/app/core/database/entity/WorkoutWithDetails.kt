package com.gymbaswara.app.core.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class WorkoutExerciseWithSets(
    @Embedded val exercise: WorkoutExerciseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "workoutExerciseId"
    )
    val sets: List<WorkoutSetEntity>
)

data class WorkoutWithDetails(
    @Embedded val workout: WorkoutEntity,
    @Relation(
        entity = WorkoutExerciseEntity::class,
        parentColumn = "id",
        entityColumn = "workoutId"
    )
    val exercises: List<WorkoutExerciseWithSets>
)
