package com.gymbaswara.app.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymbaswara.app.core.datastore.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    fun finish(onDone: () -> Unit) {
        viewModelScope.launch {
            preferencesManager.setOnboardingDone(true)
            onDone()
        }
    }
}
