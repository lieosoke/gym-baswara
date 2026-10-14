package com.gymbaswara.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "body_metrics")
data class BodyMetricEntity(
    @PrimaryKey val id: String,
    val weightKg: Double?,
    val bodyFatPercent: Double?,
    val chestCm: Double?,
    val waistCm: Double?,
    val armCm: Double?,
    val thighCm: Double?,
    val notes: String?,
    val recordedAt: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val syncStatus: String = "PENDING"
)
