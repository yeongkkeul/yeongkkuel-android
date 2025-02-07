package com.example.yeongkkuel.network.response.my

data class UserProfileResult(
    val nickname: String,
    val gender: String,
    val job: String,
    val ageGroup: String,
    val email: String,
    val profileImageUrl: String,
    val dayTargetExpenditure: Int,
    val rewardBalance: Int,
    val weeklyAchievementRate: Double
)
