package com.example.yeongkkuel.presentation.login.request

data class UserInfoRequest(
    val nickName: String,
    val gender: String,
    val ageGroup: String,
    val job: String
)
