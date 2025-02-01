package com.example.yeongkkuel.presentation.login

import com.example.yeongkkuel.presentation.login.request.UserInfoRequest
import com.example.yeongkkuel.presentation.login.response.KakaoLoginResponse
import com.example.yeongkkuel.presentation.login.response.UserInfoResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface LoginApiService {

    @GET("/api/auth/kakao-login/")
    fun kakaoLogin(
        @Query("code") code: String
    ) : Call<KakaoLoginResponse>

    @POST("/api/auth/user-info")
    suspend fun postUserInfo(
        @Body request: UserInfoRequest
    ): UserInfoResponse

}