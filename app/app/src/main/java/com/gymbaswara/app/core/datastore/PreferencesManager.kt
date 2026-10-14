package com.gymbaswara.app.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

// weightUnit is "kg" or "lb"; onboardingDone gates the first-run flow.
data class UserPreferences(
    val weightUnit: String = "kg",
    val restTimerSeconds: Int = 90,
    val onboardingDone: Boolean = false,
    val lastSyncAt: Long = 0L
)

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
        val REST_TIMER = intPreferencesKey("rest_timer_seconds")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val LAST_SYNC_AT = longPreferencesKey("last_sync_at")
    }

    val preferences: Flow<UserPreferences> = context.settingsDataStore.data.map { prefs ->
        UserPreferences(
            weightUnit = prefs[Keys.WEIGHT_UNIT] ?: "kg",
            restTimerSeconds = prefs[Keys.REST_TIMER] ?: 90,
            onboardingDone = prefs[Keys.ONBOARDING_DONE] ?: false,
            lastSyncAt = prefs[Keys.LAST_SYNC_AT] ?: 0L
        )
    }

    suspend fun setWeightUnit(unit: String) {
        context.settingsDataStore.edit { it[Keys.WEIGHT_UNIT] = unit }
    }

    suspend fun setRestTimerSeconds(seconds: Int) {
        context.settingsDataStore.edit { it[Keys.REST_TIMER] = seconds }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.settingsDataStore.edit { it[Keys.ONBOARDING_DONE] = done }
    }

    suspend fun setLastSyncAt(timestamp: Long) {
        context.settingsDataStore.edit { it[Keys.LAST_SYNC_AT] = timestamp }
    }
}
