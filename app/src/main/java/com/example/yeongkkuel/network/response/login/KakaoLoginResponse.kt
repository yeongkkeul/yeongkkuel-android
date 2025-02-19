package com.example.yeongkkuel.network.response.login

data class KakaoLoginResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: KakaoLoginResult
)

data class KakaoLoginResult(
    val accessToken: String,
    val refreshToken: String,
    val email: String,
    val redirectUrl: String,
    val userId: Int
)
