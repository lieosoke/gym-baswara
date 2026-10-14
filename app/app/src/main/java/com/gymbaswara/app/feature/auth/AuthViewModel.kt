package com.gymbaswara.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymbaswara.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(error = "Email dan kata sandi wajib diisi")
            return
        }
        _uiState.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            val result = authRepository.login(email.trim(), password)
            _uiState.value = result.fold(
                onSuccess = { AuthUiState(isSuccess = true) },
                onFailure = { AuthUiState(error = it.message ?: "Gagal masuk") }
            )
        }
    }

    fun register(name: String, email: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(error = "Semua kolom wajib diisi")
            return
        }
        if (password.length < 6) {
            _uiState.value = AuthUiState(error = "Kata sandi minimal 6 karakter")
            return
        }
        _uiState.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            val result = authRepository.register(email.trim(), password, name.trim())
            _uiState.value = result.fold(
                onSuccess = { AuthUiState(isSuccess = true) },
                onFailure = { AuthUiState(error = it.message ?: "Gagal mendaftar") }
            )
        }
    }
}
