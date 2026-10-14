package com.gymbaswara.app.core.network

import com.google.gson.annotations.SerializedName

data class SyncRequest(
    @SerializedName("workouts") val workouts: List<WorkoutDto>,
    @SerializedName("routines") val routines: List<RoutineDto>,
    @SerializedName("body_metrics") val bodyMetrics: List<BodyMetricDto>,
    @SerializedName("deleted_routines") val deletedRoutines: List<String>,
    @SerializedName("deleted_body_metrics") val deletedBodyMetrics: List<String>
)

data class SyncResponse(
    @SerializedName("data") val data: SyncData,
    @SerializedName("meta") val meta: Any?
)

data class SyncData(
    @SerializedName("success") val success: Boolean
)

data class SyncPullResponse(
    @SerializedName("data") val data: SyncPullData?,
    @SerializedName("meta") val meta: Any? = null
)

data class SyncPullData(
    @SerializedName("workouts") val workouts: List<WorkoutDto>?,
    @SerializedName("routines") val routines: List<RoutineDto>?,
    @SerializedName("body_metrics") val bodyMetrics: List<BodyMetricDto>?,
    @SerializedName("server_time") val serverTime: Long
)

data class RoutineDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("exercises") val exercises: List<RoutineExerciseDto>,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("updated_at") val updatedAt: Long
)

data class RoutineExerciseDto(
    @SerializedName("id") val id: String,
    @SerializedName("exercise_id") val exerciseId: String,
    @SerializedName("order_index") val orderIndex: Int,
    @SerializedName("notes") val notes: String?
)

data class BodyMetricDto(
    @SerializedName("id") val id: String,
    @SerializedName("weight_kg") val weightKg: Double?,
    @SerializedName("body_fat_percent") val bodyFatPercent: Double?,
    @SerializedName("chest_cm") val chestCm: Double?,
    @SerializedName("waist_cm") val waistCm: Double?,
    @SerializedName("arm_cm") val armCm: Double?,
    @SerializedName("thigh_cm") val thighCm: Double?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("recorded_at") val recordedAt: Long,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("updated_at") val updatedAt: Long
)

data class WorkoutDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("started_at") val startedAt: Long,
    @SerializedName("completed_at") val completedAt: Long?,
    @SerializedName("duration_seconds") val durationSeconds: Int,
    @SerializedName("total_volume") val totalVolume: Double,
    @SerializedName("status") val status: String,
    @SerializedName("exercises") val exercises: List<WorkoutExerciseDto>
)

data class WorkoutExerciseDto(
    @SerializedName("id") val id: String,
    @SerializedName("exercise_id") val exerciseId: String,
    @SerializedName("notes") val notes: String?,
    @SerializedName("sets") val sets: List<WorkoutSetDto>
)

data class WorkoutSetDto(
    @SerializedName("id") val id: String,
    @SerializedName("set_number") val setNumber: Int,
    @SerializedName("weight") val weight: Double,
    @SerializedName("reps") val reps: Int,
    @SerializedName("is_completed") val isCompleted: Boolean
)
