package com.gymbaswara.app.domain.model

data class BodyMetric(
    val id: String,
    val weightKg: Double?,
    val bodyFatPercent: Double?,
    val chestCm: Double?,
    val waistCm: Double?,
    val armCm: Double?,
    val thighCm: Double?,
    val notes: String?,
    val recordedAt: Long,
    val createdAt: Long,
    val updatedAt: Long
)
