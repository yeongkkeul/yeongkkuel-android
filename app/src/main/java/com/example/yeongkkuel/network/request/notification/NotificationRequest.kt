package com.example.yeongkkuel.network.request.notification

data class NotificationRequest(
    val notificationType: String,
    val content: String,
    val targetUrl: String

)
