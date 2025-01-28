package com.example.yeongkkuel.presentation.my

import com.example.yeongkkuel.presentation.my.data.UserProfileRequest
import com.example.yeongkkuel.presentation.my.data.UserProfileResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface MyPageApiService {

    @GET("/api/mypage")
    suspend fun getUserProfile(): UserProfileResponse

    @POST("/api/mypage")
    suspend fun updateUserProfile(@Body profile: UserProfileRequest): UserProfileResponse


}