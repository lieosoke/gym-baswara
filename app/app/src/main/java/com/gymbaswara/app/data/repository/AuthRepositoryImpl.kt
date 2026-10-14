package com.gymbaswara.app.data.repository

import com.gymbaswara.app.core.datastore.Session
import com.gymbaswara.app.core.datastore.SessionManager
import com.gymbaswara.app.core.network.AuthApi
import com.gymbaswara.app.core.network.AuthRequest
import com.gymbaswara.app.core.network.AuthResponse
import com.gymbaswara.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val sessionManager: SessionManager
) : AuthRepository {

    override fun session(): Flow<Session> = sessionManager.session

    override suspend fun register(email: String, password: String, name: String): Result<Unit> =
        submit { api.register(AuthRequest(email = email, password = password, name = name)) }

    override suspend fun login(email: String, password: String): Result<Unit> =
        submit { api.login(AuthRequest(email = email, password = password)) }

    override suspend fun logout() = sessionManager.clear()

    private suspend fun submit(
        call: suspend () -> Response<AuthResponse>
    ): Result<Unit> {
        return try {
            val response = call()
            val body = response.body()
            val data = body?.data
            if (response.isSuccessful && data != null) {
                sessionManager.save(data.token, data.user.id, data.user.email, data.user.name)
                Result.success(Unit)
            } else {
                val message = body?.meta?.error ?: "Permintaan gagal (kode ${response.code()})"
                Result.failure(IllegalStateException(message))
            }
        } catch (e: Exception) {
            Result.failure(IllegalStateException("Tidak dapat terhubung ke server"))
        }
    }
}
