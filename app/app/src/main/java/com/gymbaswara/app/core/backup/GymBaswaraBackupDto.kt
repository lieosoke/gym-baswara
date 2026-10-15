package com.gymbaswara.app.core.backup

import com.google.gson.annotations.SerializedName

data class GymBaswaraBackupDto(
    @SerializedName("schemaVersion") val schemaVersion: Int = 1,
    @SerializedName("appVersion") val appVersion: String,
    @SerializedName("createdAt") val createdAt: Long,
    @SerializedName("deviceModel") val deviceModel: String?,
    @SerializedName("summary") val summary: BackupSummaryDto,
    @SerializedName("workouts") val workouts: List<WorkoutBackupDto>,
    @SerializedName("routines") val routines: List<RoutineBackupDto>,
    @SerializedName("routineCrossRefs") val routineCrossRefs: List<RoutineExerciseCrossRefBackupDto>,
    @SerializedName("bodyMetrics") val bodyMetrics: List<BodyMetricBackupDto>,
    @SerializedName("customExercises") val customExercises: List<CustomExerciseBackupDto>,
    @SerializedName("preferences") val preferences: PreferencesBackupDto?
)

data class BackupSummaryDto(
    @SerializedName("workoutCount") val workoutCount: Int,
    @SerializedName("routineCount") val routineCount: Int,
    @SerializedName("metricCount") val metricCount: Int,
    @SerializedName("customExerciseCount") val customExerciseCount: Int
)

data class WorkoutBackupDto(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String?,
    @SerializedName("routineId") val routineId: String?,
    @SerializedName("routineDayId") val routineDayId: String?,
    @SerializedName("name") val name: String,
    @SerializedName("startedAt") val startedAt: Long,
    @SerializedName("completedAt") val completedAt: Long?,
    @SerializedName("durationSeconds") val durationSeconds: Int,
    @SerializedName("totalVolume") val totalVolume: Double,
    @SerializedName("status") val status: String,
    @SerializedName("createdAt") val createdAt: Long,
    @SerializedName("updatedAt") val updatedAt: Long,
    @SerializedName("exercises") val exercises: List<WorkoutExerciseBackupDto>
)

data class WorkoutExerciseBackupDto(
    @SerializedName("id") val id: String,
    @SerializedName("workoutId") val workoutId: String,
    @SerializedName("exerciseId") val exerciseId: String,
    @SerializedName("orderIndex") val orderIndex: Int,
    @SerializedName("notes") val notes: String?,
    @SerializedName("sets") val sets: List<WorkoutSetBackupDto>
)

data class WorkoutSetBackupDto(
    @SerializedName("id") val id: String,
    @SerializedName("workoutExerciseId") val workoutExerciseId: String,
    @SerializedName("setNumber") val setNumber: Int,
    @SerializedName("setType") val setType: String,
    @SerializedName("weight") val weight: Double,
    @SerializedName("reps") val reps: Int,
    @SerializedName("durationSeconds") val durationSeconds: Int?,
    @SerializedName("isCompleted") val isCompleted: Boolean
)

data class RoutineBackupDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("createdAt") val createdAt: Long,
    @SerializedName("updatedAt") val updatedAt: Long
)

data class RoutineExerciseCrossRefBackupDto(
    @SerializedName("id") val id: String,
    @SerializedName("routineId") val routineId: String,
    @SerializedName("exerciseId") val exerciseId: String,
    @SerializedName("orderIndex") val orderIndex: Int,
    @SerializedName("notes") val notes: String?
)

data class BodyMetricBackupDto(
    @SerializedName("id") val id: String,
    @SerializedName("weightKg") val weightKg: Double?,
    @SerializedName("bodyFatPercent") val bodyFatPercent: Double?,
    @SerializedName("chestCm") val chestCm: Double?,
    @SerializedName("waistCm") val waistCm: Double?,
    @SerializedName("armCm") val armCm: Double?,
    @SerializedName("thighCm") val thighCm: Double?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("recordedAt") val recordedAt: Long,
    @SerializedName("createdAt") val createdAt: Long,
    @SerializedName("updatedAt") val updatedAt: Long
)

data class CustomExerciseBackupDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("equipment") val equipment: String?,
    @SerializedName("movementType") val movementType: String?,
    @SerializedName("difficulty") val difficulty: String?,
    @SerializedName("primaryMuscle") val primaryMuscle: String?,
    @SerializedName("instructions") val instructions: String?,
    @SerializedName("createdAt") val createdAt: Long,
    @SerializedName("updatedAt") val updatedAt: Long
)

data class PreferencesBackupDto(
    @SerializedName("weightUnit") val weightUnit: String,
    @SerializedName("restTimerSeconds") val restTimerSeconds: Int
)
