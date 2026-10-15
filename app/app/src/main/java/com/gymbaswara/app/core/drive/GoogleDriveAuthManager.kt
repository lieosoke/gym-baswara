package com.gymbaswara.app.core.drive

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleDriveAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val DRIVE_APPDATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata"
    }

    private val gso: GoogleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DRIVE_APPDATA_SCOPE))
            .build()
    }

    private val client: GoogleSignInClient by lazy {
        GoogleSignIn.getClient(context, gso)
    }

    fun getSignInIntent(): Intent = client.signInIntent

    fun getCurrentAccount(): GoogleSignInAccount? {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return if (account != null && GoogleSignIn.hasPermissions(account, Scope(DRIVE_APPDATA_SCOPE))) {
            account
        } else {
            null
        }
    }

    fun parseSignInResult(data: Intent?): Result<GoogleSignInAccount> {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                Result.success(account)
            } else {
                Result.failure(IllegalStateException("Akun Google tidak ditemukan."))
            }
        } catch (e: ApiException) {
            val errorMsg = when (e.statusCode) {
                12501 -> "Masuk dengan Google dibatalkan."
                7 -> "Koneksi jaringan terputus. Pastikan internet aktif."
                else -> "Gagal menghubungkan Google Drive (Kode: ${e.statusCode})"
            }
            Result.failure(Exception(errorMsg, e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAccessToken(account: GoogleSignInAccount): Result<String> = withContext(Dispatchers.IO) {
        try {
            val scopeString = "oauth2:$DRIVE_APPDATA_SCOPE"
            val token = GoogleAuthUtil.getToken(context, account.account ?: return@withContext Result.failure(IllegalStateException("Akun tidak valid")), scopeString)
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearToken(token: String) = withContext(Dispatchers.IO) {
        try {
            GoogleAuthUtil.clearToken(context, token)
        } catch (_: Exception) {}
    }

    suspend fun signOut(): Unit = withContext(Dispatchers.IO) {
        try {
            client.signOut()
        } catch (_: Exception) {}
    }
}
