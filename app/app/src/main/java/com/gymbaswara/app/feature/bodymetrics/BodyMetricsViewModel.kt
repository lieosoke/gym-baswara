package com.gymbaswara.app.feature.bodymetrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymbaswara.app.domain.model.BodyMetric
import com.gymbaswara.app.domain.repository.BodyMetricRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BodyMetricsViewModel @Inject constructor(
    private val repository: BodyMetricRepository
) : ViewModel() {

    val metrics: StateFlow<List<BodyMetric>> = repository.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val latest: StateFlow<BodyMetric?> = repository.getLatest()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun save(
        weightKg: Double?,
        bodyFatPercent: Double?,
        chestCm: Double?,
        waistCm: Double?,
        armCm: Double?,
        thighCm: Double?,
        notes: String?
    ) {
        if (weightKg == null && bodyFatPercent == null && chestCm == null &&
            waistCm == null && armCm == null && thighCm == null
        ) return

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.save(
                BodyMetric(
                    id = UUID.randomUUID().toString(),
                    weightKg = weightKg,
                    bodyFatPercent = bodyFatPercent,
                    chestCm = chestCm,
                    waistCm = waistCm,
                    armCm = armCm,
                    thighCm = thighCm,
                    notes = notes?.ifBlank { null },
                    recordedAt = now,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
    }

    fun delete(id: String) {
        viewModelScope.launch { repository.delete(id) }
    }
}
