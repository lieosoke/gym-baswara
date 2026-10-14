package com.gymbaswara.app.domain.repository

import com.gymbaswara.app.core.datastore.Session
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun session(): Flow<Session>
    suspend fun register(email: String, password: String, name: String): Result<Unit>
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun logout()
}
