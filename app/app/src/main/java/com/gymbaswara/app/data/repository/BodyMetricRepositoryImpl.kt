package com.gymbaswara.app.data.repository

import android.content.Context
import com.gymbaswara.app.core.database.dao.BodyMetricDao
import com.gymbaswara.app.core.database.entity.BodyMetricEntity
import com.gymbaswara.app.core.sync.SyncScheduler
import com.gymbaswara.app.domain.model.BodyMetric
import com.gymbaswara.app.domain.repository.BodyMetricRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BodyMetricRepositoryImpl @Inject constructor(
    private val bodyMetricDao: BodyMetricDao,
    @ApplicationContext private val context: Context
) : BodyMetricRepository {

    override fun getAll(): Flow<List<BodyMetric>> =
        bodyMetricDao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getLatest(): Flow<BodyMetric?> =
        bodyMetricDao.getLatest().map { it?.toDomain() }

    override suspend fun save(metric: BodyMetric) {
        bodyMetricDao.upsert(metric.toEntity())
        SyncScheduler.enqueue(context)
    }

    override suspend fun delete(id: String) {
        if (bodyMetricDao.getSyncStatus(id) == "SYNCED") {
            bodyMetricDao.markPendingDelete(id)
        } else {
            bodyMetricDao.delete(id)
        }
        SyncScheduler.enqueue(context)
    }

    private fun BodyMetricEntity.toDomain() = BodyMetric(
        id = id,
        weightKg = weightKg,
        bodyFatPercent = bodyFatPercent,
        chestCm = chestCm,
        waistCm = waistCm,
        armCm = armCm,
        thighCm = thighCm,
        notes = notes,
        recordedAt = recordedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun BodyMetric.toEntity(syncStatus: String = "PENDING") = BodyMetricEntity(
        id = id,
        weightKg = weightKg,
        bodyFatPercent = bodyFatPercent,
        chestCm = chestCm,
        waistCm = waistCm,
        armCm = armCm,
        thighCm = thighCm,
        notes = notes,
        recordedAt = recordedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = syncStatus
    )
}
