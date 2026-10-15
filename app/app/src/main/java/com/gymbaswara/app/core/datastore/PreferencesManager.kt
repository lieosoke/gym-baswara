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
    val lastSyncAt: Long = 0L,
    val lastDriveBackupAt: Long = 0L,
    val lastDriveBackupSize: Long = 0L,
    val driveConnectedEmail: String? = null
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
        val LAST_DRIVE_BACKUP_AT = longPreferencesKey("last_drive_backup_at")
        val LAST_DRIVE_BACKUP_SIZE = longPreferencesKey("last_drive_backup_size")
        val DRIVE_CONNECTED_EMAIL = stringPreferencesKey("drive_connected_email")
    }

    val preferences: Flow<UserPreferences> = context.settingsDataStore.data.map { prefs ->
        UserPreferences(
            weightUnit = prefs[Keys.WEIGHT_UNIT] ?: "kg",
            restTimerSeconds = prefs[Keys.REST_TIMER] ?: 90,
            onboardingDone = prefs[Keys.ONBOARDING_DONE] ?: false,
            lastSyncAt = prefs[Keys.LAST_SYNC_AT] ?: 0L,
            lastDriveBackupAt = prefs[Keys.LAST_DRIVE_BACKUP_AT] ?: 0L,
            lastDriveBackupSize = prefs[Keys.LAST_DRIVE_BACKUP_SIZE] ?: 0L,
            driveConnectedEmail = prefs[Keys.DRIVE_CONNECTED_EMAIL]
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

    suspend fun setDriveBackupInfo(timestamp: Long, sizeBytes: Long, email: String?) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.LAST_DRIVE_BACKUP_AT] = timestamp
            prefs[Keys.LAST_DRIVE_BACKUP_SIZE] = sizeBytes
            if (email != null) {
                prefs[Keys.DRIVE_CONNECTED_EMAIL] = email
            }
        }
    }

    suspend fun setDriveConnectedEmail(email: String?) {
        context.settingsDataStore.edit { prefs ->
            if (email != null) {
                prefs[Keys.DRIVE_CONNECTED_EMAIL] = email
            } else {
                prefs.remove(Keys.DRIVE_CONNECTED_EMAIL)
            }
        }
    }
}
