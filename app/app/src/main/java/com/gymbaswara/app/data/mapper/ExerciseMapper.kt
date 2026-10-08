package com.gymbaswara.app.data.mapper

import com.gymbaswara.app.core.database.entity.ExerciseEntity
import com.gymbaswara.app.domain.model.Exercise

fun ExerciseEntity.toDomain(): Exercise {
    return Exercise(
        id = id,
        name = name,
        description = description,
        equipment = equipment,
        movementType = movementType,
        difficulty = difficulty,
        primaryMuscle = primaryMuscle,
        instructions = instructions,
        imageUrl = imageUrl,
        isSystem = isSystem
    )
}

fun List<ExerciseEntity>.toDomain(): List<Exercise> {
    return this.map { it.toDomain() }
}
