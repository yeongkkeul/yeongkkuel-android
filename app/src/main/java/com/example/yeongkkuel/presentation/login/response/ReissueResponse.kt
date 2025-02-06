package com.example.yeongkkuel.presentation.login.response

data class ReissueResponse(
    val grantType: String,
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresIn: Int
)
