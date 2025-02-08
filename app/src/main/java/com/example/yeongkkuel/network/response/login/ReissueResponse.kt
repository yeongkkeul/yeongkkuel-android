package com.example.yeongkkuel.network.response.login

data class ReissueResponse(
    val grantType: String,
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresIn: Int
)
