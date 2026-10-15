package com.gymbaswara.app.core.drive

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class DriveFileMetadata(
    val id: String,
    val name: String,
    val sizeBytes: Long,
    val modifiedTimeMillis: Long
)

@Singleton
class GoogleDriveService @Inject constructor() {

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val gson: Gson = Gson()

    companion object {
        const val BACKUP_FILE_NAME = "gymbaswara_backup.json.gz"
        private const val DRIVE_FILES_ENDPOINT = "https://www.googleapis.com/drive/v3/files"
        private const val DRIVE_UPLOAD_ENDPOINT = "https://www.googleapis.com/upload/drive/v3/files"
        private val MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=UTF-8")
        private val MEDIA_TYPE_GZIP = MediaType.parse("application/gzip")
    }

    private data class DriveFileListResponse(
        @SerializedName("files") val files: List<DriveFileItem>?
    )

    private data class DriveFileItem(
        @SerializedName("id") val id: String,
        @SerializedName("name") val name: String,
        @SerializedName("size") val size: String?,
        @SerializedName("modifiedTime") val modifiedTime: String?
    )

    suspend fun getBackupMetadata(token: String): Result<DriveFileMetadata?> = withContext(Dispatchers.IO) {
        try {
            val url = "$DRIVE_FILES_ENDPOINT?spaces=appDataFolder&q=name='$BACKUP_FILE_NAME' and trashed=false&fields=files(id,name,size,modifiedTime)"
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val code = response.code()
                val msg = response.message()
                response.close()
                return@withContext Result.failure(Exception("Gagal memeriksa Google Drive: $code $msg"))
            }

            val bodyString = response.body()?.string().orEmpty()
            response.close()
            val listResponse = gson.fromJson(bodyString, DriveFileListResponse::class.java)
            val fileItem = listResponse.files?.firstOrNull()

            if (fileItem == null) {
                Result.success(null)
            } else {
                val sizeBytes = fileItem.size?.toLongOrNull() ?: 0L
                val modifiedMillis = parseIso8601(fileItem.modifiedTime)
                Result.success(
                    DriveFileMetadata(
                        id = fileItem.id,
                        name = fileItem.name,
                        sizeBytes = sizeBytes,
                        modifiedTimeMillis = modifiedMillis
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadBackup(token: String, data: ByteArray): Result<DriveFileMetadata> = withContext(Dispatchers.IO) {
        try {
            val existing = getBackupMetadata(token).getOrNull()

            val request = if (existing != null) {
                val uploadUrl = "$DRIVE_UPLOAD_ENDPOINT/${existing.id}?uploadType=media"
                val body = RequestBody.create(MEDIA_TYPE_GZIP, data)
                Request.Builder()
                    .url(uploadUrl)
                    .addHeader("Authorization", "Bearer $token")
                    .patch(body)
                    .build()
            } else {
                val uploadUrl = "$DRIVE_UPLOAD_ENDPOINT?uploadType=multipart"
                val metadataJson = """{"name":"$BACKUP_FILE_NAME","parents":["appDataFolder"]}"""
                val metadataBody = RequestBody.create(MEDIA_TYPE_JSON, metadataJson)
                val fileBody = RequestBody.create(MEDIA_TYPE_GZIP, data)

                val multipartBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("metadata", null, metadataBody)
                    .addFormDataPart("file", BACKUP_FILE_NAME, fileBody)
                    .build()

                Request.Builder()
                    .url(uploadUrl)
                    .addHeader("Authorization", "Bearer $token")
                    .post(multipartBody)
                    .build()
            }

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val code = response.code()
                val msg = response.message()
                response.close()
                return@withContext Result.failure(Exception("Gagal mengunggah ke Google Drive: $code $msg"))
            }

            val bodyString = response.body()?.string().orEmpty()
            response.close()
            val createdItem = gson.fromJson(bodyString, DriveFileItem::class.java)

            Result.success(
                DriveFileMetadata(
                    id = createdItem?.id ?: existing?.id.orEmpty(),
                    name = BACKUP_FILE_NAME,
                    sizeBytes = data.size.toLong(),
                    modifiedTimeMillis = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadBackup(token: String, fileId: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val url = "$DRIVE_FILES_ENDPOINT/$fileId?alt=media"
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val code = response.code()
                val msg = response.message()
                response.close()
                return@withContext Result.failure(Exception("Gagal mengunduh cadangan dari Google Drive: $code $msg"))
            }

            val bytes = response.body()?.bytes()
            response.close()
            if (bytes != null && bytes.isNotEmpty()) {
                Result.success(bytes)
            } else {
                Result.failure(IllegalStateException("File cadangan kosong atau tidak ditemukan."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseIso8601(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return System.currentTimeMillis()
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            format.parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (_: Exception) {
            try {
                val fallbackFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                fallbackFormat.parse(dateStr)?.time ?: System.currentTimeMillis()
            } catch (_: Exception) {
                System.currentTimeMillis()
            }
        }
    }
}
