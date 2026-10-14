package com.gymbaswara.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymbaswara.app.core.datastore.PreferencesManager
import com.gymbaswara.app.core.datastore.Session
import com.gymbaswara.app.domain.repository.AuthRepository
import com.gymbaswara.app.feature.workout.WorkoutSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    sessionManager: WorkoutSessionManager,
    authRepository: AuthRepository,
    preferencesManager: PreferencesManager
) : ViewModel() {
    val isActiveSession = sessionManager.isActive

    val session: StateFlow<Session> = authRepository.session()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = Session(token = null, userId = null, email = null, name = null)
        )

    val onboardingDone: StateFlow<Boolean?> = preferencesManager.preferences
        .map { it.onboardingDone }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )
}
