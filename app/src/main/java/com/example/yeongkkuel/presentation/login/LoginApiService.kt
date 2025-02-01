package com.example.yeongkkuel.presentation.login

import com.example.yeongkkuel.presentation.login.request.ReferralRequest
import com.example.yeongkkuel.presentation.login.request.TermsAgreeRequest
import com.example.yeongkkuel.presentation.login.request.UserInfoRequest
import com.example.yeongkkuel.presentation.login.response.KakaoLoginResponse
import com.example.yeongkkuel.presentation.login.response.ReferralResponse
import com.example.yeongkkuel.presentation.login.response.TermsAgreeResponse
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
        @Query("accessToken") Token: String
    ) : Call<KakaoLoginResponse>

    @POST("/api/auth/user-info")
    suspend fun postUserInfo(
        @Body request: UserInfoRequest
    ): UserInfoResponse

    @POST("/api/api/recommend-code")
    fun validateRecommendCode(
        @Body request: ReferralRequest
    ): ReferralResponse

    @POST("/api/auth/term-agreement")
    fun agreeTerms(
        @Body request: TermsAgreeRequest
    ): Call<TermsAgreeResponse>

}