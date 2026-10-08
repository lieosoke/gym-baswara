package com.gymbaswara.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String?,
    val equipment: String?,
    val movementType: String?,
    val difficulty: String?,
    val primaryMuscle: String?,
    val instructions: String?,
    val imageUrl: String?,
    val isSystem: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
