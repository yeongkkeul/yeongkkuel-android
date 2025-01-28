package com.example.yeongkkuel.presentation.my.data

data class UserProfileRequest(
    val nickname: String,
    val gender: String,
    val age_group: String,
    val job: String,
    val profileImageUrl: String
)