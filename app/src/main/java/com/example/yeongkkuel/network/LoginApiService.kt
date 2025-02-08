package com.example.yeongkkuel.network

import com.example.yeongkkuel.network.request.login.ReferralRequest
import com.example.yeongkkuel.network.request.login.TermsAgreeRequest
import com.example.yeongkkuel.network.request.login.UserInfoRequest
import com.example.yeongkkuel.network.response.login.KakaoLoginResponse
import com.example.yeongkkuel.network.response.login.ReferralResponse
import com.example.yeongkkuel.network.response.login.TermsAgreeResponse
import com.example.yeongkkuel.network.response.login.UserInfoResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface LoginApiService {

    @GET("/api/auth/kakao-login/")
    fun kakaoLogin(
        @Query("accessToken") Token: String
    ) : Call<KakaoLoginResponse>

    @PUT("/api/auth/user-info")
    suspend fun postUserInfo(
        @Body request: UserInfoRequest
    ): UserInfoResponse

    @POST("/api/api/recommend-code")
    suspend fun validateRecommendCode(
        @Body request: ReferralRequest
    ): ReferralResponse

    @POST("/api/auth/term-agreement")
    fun agreeTerms(
        @Body request: TermsAgreeRequest
    ): Call<TermsAgreeResponse>

}