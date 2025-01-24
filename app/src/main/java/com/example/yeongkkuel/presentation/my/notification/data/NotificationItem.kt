package com.example.yeongkkuel.presentation.my.notification.data

data class NotificationItem(
    val type: NotificationType,
    val message: String,
    val timeText: String,
    val section: String // "오늘", "어제", "최근 7일" 등
)
