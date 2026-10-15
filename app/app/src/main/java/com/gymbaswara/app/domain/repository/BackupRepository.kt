package com.gymbaswara.app.domain.repository

import android.content.Intent
import com.gymbaswara.app.core.backup.GymBaswaraBackupDto
import com.gymbaswara.app.core.backup.RestoreResult
import com.gymbaswara.app.core.drive.DriveFileMetadata
import kotlinx.coroutines.flow.Flow
import java.io.InputStream
import java.io.OutputStream

data class CloudBackupInfo(
    val isConnected: Boolean,
    val connectedEmail: String?,
    val remoteFile: DriveFileMetadata?,
    val localLastBackupAt: Long,
    val localLastBackupSize: Long
)

interface BackupRepository {
    fun getCloudBackupInfo(): Flow<CloudBackupInfo>
    fun getSignInIntent(): Intent
    suspend fun handleSignInResult(data: Intent?): Result<String>
    suspend fun disconnectDrive(): Result<Unit>
    suspend fun backupToGoogleDrive(): Result<DriveFileMetadata>
    suspend fun restoreFromGoogleDrive(): Result<RestoreResult>
    suspend fun exportToStream(outputStream: OutputStream): Result<Unit>
    suspend fun importFromStream(inputStream: InputStream): Result<RestoreResult>
    suspend fun previewBackupFromStream(inputStream: InputStream): Result<GymBaswaraBackupDto>
    suspend fun refreshCloudInfo(): Result<DriveFileMetadata?>
}
