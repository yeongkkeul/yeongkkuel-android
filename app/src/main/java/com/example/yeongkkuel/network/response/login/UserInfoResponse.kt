package com.example.yeongkkuel.network.response.login

data class UserInfoResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: Any
)
