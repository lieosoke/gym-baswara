package com.gymbaswara.app.core.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class AuthRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("name") val name: String? = null
)

data class AuthUserDto(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String
)

data class AuthData(
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: AuthUserDto
)

data class ApiMeta(
    @SerializedName("error") val error: String? = null
)

data class AuthResponse(
    @SerializedName("data") val data: AuthData?,
    @SerializedName("meta") val meta: ApiMeta? = null
)

interface AuthApi {
    @POST("api/v1/auth/register")
    suspend fun register(@Body request: AuthRequest): Response<AuthResponse>

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>
}
