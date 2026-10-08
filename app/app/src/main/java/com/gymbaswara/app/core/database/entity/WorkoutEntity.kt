package com.gymbaswara.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val routineId: String?,
    val routineDayId: String?,
    val name: String,
    val startedAt: Long,
    val completedAt: Long?,
    val durationSeconds: Int,
    val totalVolume: Double,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long
)
