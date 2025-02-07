package com.example.yeongkkuel.network.response.my

data class UserProfileResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: UserProfileResult?
)
