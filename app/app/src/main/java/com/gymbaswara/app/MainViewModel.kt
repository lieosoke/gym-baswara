package com.gymbaswara.app

import androidx.lifecycle.ViewModel
import com.gymbaswara.app.feature.workout.WorkoutSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    sessionManager: WorkoutSessionManager
) : ViewModel() {
    val isActiveSession = sessionManager.isActive
}
