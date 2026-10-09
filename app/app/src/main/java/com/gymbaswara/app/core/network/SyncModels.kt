package com.gymbaswara.app.core.network

import com.google.gson.annotations.SerializedName

data class SyncRequest(
    @SerializedName("workouts") val workouts: List<WorkoutDto>
)

data class SyncResponse(
    @SerializedName("data") val data: SyncData,
    @SerializedName("meta") val meta: Any?
)

data class SyncData(
    @SerializedName("success") val success: Boolean
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
