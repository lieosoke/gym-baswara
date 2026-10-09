package com.gymbaswara.app.domain.model

data class Routine(
    val id: String,
    val name: String,
    val description: String?,
    val exercises: List<RoutineExercise>,
    val createdAt: Long,
    val updatedAt: Long
)
