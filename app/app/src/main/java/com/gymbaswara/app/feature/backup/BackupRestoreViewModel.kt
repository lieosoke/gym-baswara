package com.gymbaswara.app.feature.backup

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymbaswara.app.core.backup.RestoreResult
import com.gymbaswara.app.domain.repository.BackupRepository
import com.gymbaswara.app.domain.repository.CloudBackupInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

data class BackupRestoreUiState(
    val isLoading: Boolean = false,
    val loadingMessage: String? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val showRestoreConfirmDialog: Boolean = false,
    val pendingRestoreSource: String? = null // "DRIVE" or "LOCAL"
)

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    private val backupRepository: BackupRepository
) : ViewModel() {

    val cloudInfo: StateFlow<CloudBackupInfo> = backupRepository.getCloudBackupInfo()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CloudBackupInfo(
                isConnected = false,
                connectedEmail = null,
                remoteFile = null,
                localLastBackupAt = 0L,
                localLastBackupSize = 0L
            )
        )

    private val _uiState = MutableStateFlow(BackupRestoreUiState())
    val uiState: StateFlow<BackupRestoreUiState> = _uiState.asStateFlow()

    init {
        refreshCloudMetadata()
    }

    fun getSignInIntent(): Intent = backupRepository.getSignInIntent()

    fun handleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingMessage = "Menghubungkan akun Google Drive...") }
            val result = backupRepository.handleSignInResult(data)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadingMessage = null,
                        successMessage = "Google Drive terhubung: ${result.getOrNull()}"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadingMessage = null,
                        errorMessage = result.exceptionOrNull()?.message ?: "Gagal menghubungkan Google Drive."
                    )
                }
            }
        }
    }

    fun refreshCloudMetadata() {
        viewModelScope.launch {
            backupRepository.refreshCloudInfo()
        }
    }

    fun disconnectDrive() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingMessage = "Memutuskan akun Google Drive...") }
            backupRepository.disconnectDrive()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    loadingMessage = null,
                    successMessage = "Akun Google Drive berhasil diputuskan."
                )
            }
        }
    }

    fun backupToDrive() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingMessage = "Mengunggah cadangan ke Google Drive...") }
            val result = backupRepository.backupToGoogleDrive()
            if (result.isSuccess) {
                val meta = result.getOrThrow()
                val kb = meta.sizeBytes / 1024
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadingMessage = null,
                        successMessage = "Cadangan berhasil disimpan ke Google Drive (${kb} KB)."
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadingMessage = null,
                        errorMessage = result.exceptionOrNull()?.message ?: "Gagal mencadangkan ke Google Drive."
                    )
                }
            }
        }
    }

    fun requestRestoreFromDrive() {
        _uiState.update {
            it.copy(
                showRestoreConfirmDialog = true,
                pendingRestoreSource = "DRIVE"
            )
        }
    }

    fun dismissRestoreDialog() {
        _uiState.update {
            it.copy(
                showRestoreConfirmDialog = false,
                pendingRestoreSource = null
            )
        }
    }

    fun confirmRestoreFromDrive() {
        dismissRestoreDialog()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingMessage = "Mengunduh dan memulihkan data dari Google Drive...") }
            val result = backupRepository.restoreFromGoogleDrive()
            if (result.isSuccess) {
                val res = result.getOrThrow()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadingMessage = null,
                        successMessage = "Berhasil memulihkan ${res.workoutCount} latihan, ${res.routineCount} rutinitas, dan ${res.metricCount} data tubuh."
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadingMessage = null,
                        errorMessage = result.exceptionOrNull()?.message ?: "Gagal memulihkan cadangan dari Google Drive."
                    )
                }
            }
        }
    }

    fun exportToLocalFile(outputStream: OutputStream) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingMessage = "Menyimpan file cadangan...") }
            val result = backupRepository.exportToStream(outputStream)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadingMessage = null,
                        successMessage = "File cadangan lokal berhasil disimpan."
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadingMessage = null,
                        errorMessage = result.exceptionOrNull()?.message ?: "Gagal mengekspor file cadangan."
                    )
                }
            }
        }
    }

    fun importFromLocalFile(inputStream: InputStream) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingMessage = "Membaca dan memulihkan file cadangan...") }
            val result = backupRepository.importFromStream(inputStream)
            if (result.isSuccess) {
                val res = result.getOrThrow()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadingMessage = null,
                        successMessage = "Berhasil memulihkan ${res.workoutCount} latihan dan ${res.routineCount} rutinitas dari file lokal."
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadingMessage = null,
                        errorMessage = result.exceptionOrNull()?.message ?: "Gagal memulihkan data dari file lokal."
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}
