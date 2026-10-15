package com.gymbaswara.app.data.repository

import android.content.Intent
import com.gymbaswara.app.core.backup.BackupManager
import com.gymbaswara.app.core.backup.GymBaswaraBackupDto
import com.gymbaswara.app.core.backup.RestoreManager
import com.gymbaswara.app.core.backup.RestoreResult
import com.gymbaswara.app.core.datastore.PreferencesManager
import com.gymbaswara.app.core.drive.DriveFileMetadata
import com.gymbaswara.app.core.drive.GoogleDriveAuthManager
import com.gymbaswara.app.core.drive.GoogleDriveService
import com.gymbaswara.app.domain.repository.BackupRepository
import com.gymbaswara.app.domain.repository.CloudBackupInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepositoryImpl @Inject constructor(
    private val backupManager: BackupManager,
    private val restoreManager: RestoreManager,
    private val googleDriveAuthManager: GoogleDriveAuthManager,
    private val googleDriveService: GoogleDriveService,
    private val preferencesManager: PreferencesManager
) : BackupRepository {

    private val remoteFileState = MutableStateFlow<DriveFileMetadata?>(null)

    override fun getCloudBackupInfo(): Flow<CloudBackupInfo> {
        return combine(
            preferencesManager.preferences,
            remoteFileState
        ) { prefs, remoteFile ->
            val account = googleDriveAuthManager.getCurrentAccount()
            val isConnected = account != null || !prefs.driveConnectedEmail.isNullOrBlank()
            val email = account?.email ?: prefs.driveConnectedEmail

            CloudBackupInfo(
                isConnected = isConnected,
                connectedEmail = email,
                remoteFile = remoteFile,
                localLastBackupAt = prefs.lastDriveBackupAt,
                localLastBackupSize = prefs.lastDriveBackupSize
            )
        }
    }

    override fun getSignInIntent(): Intent {
        return googleDriveAuthManager.getSignInIntent()
    }

    override suspend fun handleSignInResult(data: Intent?): Result<String> = withContext(Dispatchers.IO) {
        val result = googleDriveAuthManager.parseSignInResult(data)
        if (result.isFailure) {
            return@withContext Result.failure(result.exceptionOrNull() ?: Exception("Gagal masuk dengan Google."))
        }

        val account = result.getOrThrow()
        val email = account.email.orEmpty()
        preferencesManager.setDriveConnectedEmail(email)

        // Muat info file cadangan jika sudah ada
        refreshCloudInfo()

        Result.success(email)
    }

    override suspend fun disconnectDrive(): Result<Unit> = withContext(Dispatchers.IO) {
        googleDriveAuthManager.signOut()
        preferencesManager.setDriveConnectedEmail(null)
        remoteFileState.value = null
        Result.success(Unit)
    }

    override suspend fun refreshCloudInfo(): Result<DriveFileMetadata?> = withContext(Dispatchers.IO) {
        val account = googleDriveAuthManager.getCurrentAccount()
            ?: return@withContext Result.failure(IllegalStateException("Belum terhubung ke Google Drive."))

        val tokenResult = googleDriveAuthManager.getAccessToken(account)
        if (tokenResult.isFailure) {
            return@withContext Result.failure(tokenResult.exceptionOrNull() ?: Exception("Gagal mengambil izin akses."))
        }

        val token = tokenResult.getOrThrow()
        val metaResult = googleDriveService.getBackupMetadata(token)
        if (metaResult.isSuccess) {
            val meta = metaResult.getOrNull()
            remoteFileState.value = meta
            if (meta != null) {
                preferencesManager.setDriveBackupInfo(meta.modifiedTimeMillis, meta.sizeBytes, account.email)
            }
            Result.success(meta)
        } else {
            Result.failure(metaResult.exceptionOrNull() ?: Exception("Gagal memeriksa Google Drive."))
        }
    }

    override suspend fun backupToGoogleDrive(): Result<DriveFileMetadata> = withContext(Dispatchers.IO) {
        val account = googleDriveAuthManager.getCurrentAccount()
            ?: return@withContext Result.failure(IllegalStateException("Akun Google Drive belum terhubung. Silakan hubungkan akun terlebih dahulu."))

        val tokenResult = googleDriveAuthManager.getAccessToken(account)
        if (tokenResult.isFailure) {
            return@withContext Result.failure(tokenResult.exceptionOrNull() ?: Exception("Gagal mendapatkan token Google Drive."))
        }

        val token = tokenResult.getOrThrow()
        val backupBytes = backupManager.createGzipBackupBytes()

        val uploadResult = googleDriveService.uploadBackup(token, backupBytes)
        if (uploadResult.isSuccess) {
            val meta = uploadResult.getOrThrow()
            remoteFileState.value = meta
            preferencesManager.setDriveBackupInfo(meta.modifiedTimeMillis, meta.sizeBytes, account.email)
            Result.success(meta)
        } else {
            Result.failure(uploadResult.exceptionOrNull() ?: Exception("Gagal mencadangkan data ke Google Drive."))
        }
    }

    override suspend fun restoreFromGoogleDrive(): Result<RestoreResult> = withContext(Dispatchers.IO) {
        val account = googleDriveAuthManager.getCurrentAccount()
            ?: return@withContext Result.failure(IllegalStateException("Akun Google Drive belum terhubung."))

        val tokenResult = googleDriveAuthManager.getAccessToken(account)
        if (tokenResult.isFailure) {
            return@withContext Result.failure(tokenResult.exceptionOrNull() ?: Exception("Gagal mendapatkan izin akses."))
        }

        val token = tokenResult.getOrThrow()
        val metaResult = googleDriveService.getBackupMetadata(token)
        val fileId = metaResult.getOrNull()?.id
            ?: return@withContext Result.failure(IllegalStateException("Tidak ditemukan file cadangan di Google Drive akun ini."))

        val downloadResult = googleDriveService.downloadBackup(token, fileId)
        if (downloadResult.isFailure) {
            return@withContext Result.failure(downloadResult.exceptionOrNull() ?: Exception("Gagal mengunduh cadangan."))
        }

        val bytes = downloadResult.getOrThrow()
        val parseResult = restoreManager.parseBackup(ByteArrayInputStream(bytes))
        if (parseResult.isFailure) {
            return@withContext Result.failure(parseResult.exceptionOrNull() ?: Exception("File cadangan rusak."))
        }

        val backup = parseResult.getOrThrow()
        restoreManager.restoreBackup(backup)
    }

    override suspend fun exportToStream(outputStream: OutputStream): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val backup = backupManager.createBackup()
            backupManager.writeBackupToGzipStream(backup, outputStream)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun importFromStream(inputStream: InputStream): Result<RestoreResult> = withContext(Dispatchers.IO) {
        try {
            val parseResult = restoreManager.parseBackup(inputStream)
            if (parseResult.isFailure) {
                return@withContext Result.failure(parseResult.exceptionOrNull() ?: Exception("Format file tidak valid."))
            }
            restoreManager.restoreBackup(parseResult.getOrThrow())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun previewBackupFromStream(inputStream: InputStream): Result<GymBaswaraBackupDto> = withContext(Dispatchers.IO) {
        restoreManager.parseBackup(inputStream)
    }
}
