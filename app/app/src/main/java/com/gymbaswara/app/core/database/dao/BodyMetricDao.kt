package com.gymbaswara.app.core.database.dao

import androidx.room.*
import com.gymbaswara.app.core.database.entity.BodyMetricEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyMetricDao {

    @Query("SELECT * FROM body_metrics WHERE syncStatus != 'PENDING_DELETE' ORDER BY recordedAt DESC")
    fun getAll(): Flow<List<BodyMetricEntity>>

    @Query("SELECT * FROM body_metrics WHERE syncStatus != 'PENDING_DELETE' ORDER BY recordedAt DESC")
    suspend fun getAllOnce(): List<BodyMetricEntity>

    @Query("SELECT * FROM body_metrics WHERE syncStatus != 'PENDING_DELETE' ORDER BY recordedAt DESC LIMIT 1")
    fun getLatest(): Flow<BodyMetricEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(metric: BodyMetricEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(metrics: List<BodyMetricEntity>)

    @Query("SELECT * FROM body_metrics WHERE syncStatus = 'PENDING'")
    suspend fun getPending(): List<BodyMetricEntity>

    @Query("SELECT id FROM body_metrics WHERE syncStatus = 'PENDING_DELETE'")
    suspend fun getPendingDeleteIds(): List<String>

    @Query("SELECT syncStatus FROM body_metrics WHERE id = :id")
    suspend fun getSyncStatus(id: String): String?

    @Query("UPDATE body_metrics SET syncStatus = 'SYNCED' WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<String>)

    @Query("UPDATE body_metrics SET syncStatus = 'PENDING_DELETE' WHERE id = :id")
    suspend fun markPendingDelete(id: String)

    @Query("DELETE FROM body_metrics WHERE id IN (:ids)")
    suspend fun hardDeleteByIds(ids: List<String>)

    @Query("DELETE FROM body_metrics WHERE id = :id")
    suspend fun delete(id: String)
}
