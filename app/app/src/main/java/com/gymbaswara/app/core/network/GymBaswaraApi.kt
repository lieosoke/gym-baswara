package com.gymbaswara.app.core.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface GymBaswaraApi {
    
    @POST("api/v1/sync/push")
    suspend fun pushData(
        @Body request: SyncRequest
    ): Response<SyncResponse>
}
