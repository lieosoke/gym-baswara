package com.gymbaswara.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymbaswara.app.core.datastore.PreferencesManager
import com.gymbaswara.app.core.datastore.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = preferencesManager.preferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserPreferences()
        )

    fun setWeightUnit(unit: String) {
        viewModelScope.launch { preferencesManager.setWeightUnit(unit) }
    }

    fun setRestTimerSeconds(seconds: Int) {
        viewModelScope.launch { preferencesManager.setRestTimerSeconds(seconds.coerceIn(15, 600)) }
    }
}
