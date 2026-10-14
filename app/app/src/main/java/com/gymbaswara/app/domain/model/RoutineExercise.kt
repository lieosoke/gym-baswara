package com.gymbaswara.app.domain.model

import java.util.UUID

data class RoutineExercise(
    val instanceId: String = UUID.randomUUID().toString(),
    val exercise: Exercise,
    val notes: String? = null
)
