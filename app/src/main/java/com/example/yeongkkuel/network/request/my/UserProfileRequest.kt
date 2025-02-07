package com.example.yeongkkuel.network.request.my

data class UserProfileRequest(
    val nickname: String,
    val gender: String,
    val age_group: String,
    val job: String,
    val profileImageUrl: String
)