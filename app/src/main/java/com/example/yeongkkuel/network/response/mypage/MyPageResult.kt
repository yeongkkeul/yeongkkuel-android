package com.example.yeongkkuel.network.response.mypage

import com.google.gson.annotations.SerializedName

data class MyPageResult(
    @SerializedName("nickname") val nickname: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("job") val job: String,
    @SerializedName("ageGroup") val ageGroup: String,
    @SerializedName("email") val email: String,
    @SerializedName("profileImageUrl") val profileImageUrl: String,
    @SerializedName("dayTargetExpenditure") val dayTargetExpenditure: Int,
    @SerializedName("rewardBalance") val rewardBalance: Int,
    @SerializedName("weeklyAchievementRate") val weeklyAchievementRate: Double
)