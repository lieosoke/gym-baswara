package com.gymbaswara.app.domain.repository

import com.gymbaswara.app.domain.model.BodyMetric
import kotlinx.coroutines.flow.Flow

interface BodyMetricRepository {
    fun getAll(): Flow<List<BodyMetric>>
    fun getLatest(): Flow<BodyMetric?>
    suspend fun save(metric: BodyMetric)
    suspend fun delete(id: String)
}
