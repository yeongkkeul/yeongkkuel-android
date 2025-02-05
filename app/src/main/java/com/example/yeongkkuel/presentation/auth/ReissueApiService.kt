package com.example.yeongkkuel.presentation.auth

import com.example.yeongkkuel.presentation.login.request.ReissueRequest
import com.example.yeongkkuel.presentation.login.response.ReissueResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ReissueApiService {

    @POST("/api/reissue")
    fun reissueToken(
        @Body request: ReissueRequest
    ): Call<ReissueResponse>
}