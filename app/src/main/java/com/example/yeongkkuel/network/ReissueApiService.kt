package com.example.yeongkkuel.network

import com.example.yeongkkuel.network.request.login.ReissueRequest
import com.example.yeongkkuel.network.response.login.ReissueResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ReissueApiService {

    @POST("/api/auth/reissue")
    suspend fun reissueToken(
        @Body request: ReissueRequest
    ): Call<ReissueResponse>
}