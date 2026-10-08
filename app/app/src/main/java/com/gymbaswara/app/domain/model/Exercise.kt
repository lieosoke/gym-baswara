package com.gymbaswara.app.domain.model

data class Exercise(
    val id: String,
    val name: String,
    val description: String?,
    val equipment: String?,
    val movementType: String?,
    val difficulty: String?,
    val primaryMuscle: String?,
    val instructions: String?,
    val imageUrl: String?,
    val isSystem: Boolean
)
