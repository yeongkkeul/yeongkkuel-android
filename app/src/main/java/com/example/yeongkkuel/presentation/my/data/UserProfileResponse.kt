package com.example.yeongkkuel.presentation.my.data

data class UserProfileResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: UserProfileResult?
)
