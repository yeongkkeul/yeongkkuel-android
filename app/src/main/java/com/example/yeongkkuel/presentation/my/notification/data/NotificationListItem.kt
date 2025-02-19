package com.example.yeongkkuel.presentation.my.notification.data

sealed class NotificationListItem {
    // 헤더(섹션 타이틀)용
    data class HeaderItem(val sectionName: String) : NotificationListItem()

    // 일반 알림 아이템용
    data class NormalItem(val notification: NotificationItem) : NotificationListItem()
}