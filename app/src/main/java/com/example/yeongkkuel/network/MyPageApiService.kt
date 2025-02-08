package com.example.yeongkkuel.network

import com.example.yeongkkuel.network.request.my.UserProfileRequest
import com.example.yeongkkuel.network.response.my.UserProfileResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface MyPageApiService {

    @GET("/api/mypage")
    suspend fun getUserProfile(): UserProfileResponse

    @POST("/api/mypage")
    suspend fun updateUserProfile(@Body profile: UserProfileRequest): UserProfileResponse


}