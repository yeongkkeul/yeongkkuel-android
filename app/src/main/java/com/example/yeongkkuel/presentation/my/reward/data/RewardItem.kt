package com.example.yeongkkuel.presentation.my.reward.data

import com.example.yeongkkuel.presentation.my.notification.data.NotificationType


data class RewardItem(
    val type: RewardType,
    val message: String,
    val rewardText: String,
    val section: String // "오늘", "어제", "최근 7일" 등
)
