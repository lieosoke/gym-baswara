package com.gymbaswara.app.core.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface GymBaswaraApi {

    @POST("api/v1/sync/push")
    suspend fun pushData(
        @Body request: SyncRequest
    ): Response<SyncResponse>

    @GET("api/v1/sync/pull")
    suspend fun pullData(
        @Query("since") since: Long
    ): Response<SyncPullResponse>
}
