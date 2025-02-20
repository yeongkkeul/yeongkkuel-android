package com.example.yeongkkuel.network.response.notification

data class NotificationDetail(
    val id: Long,
    val notificationType: String,
    val notificationContent: String,
    val targetUrl: String,
    val isRead: Boolean,
    val timestamp: String,
    val createdAt: String
)
