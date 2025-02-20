package com.example.yeongkkuel.presentation.my.reward.data

sealed class RewardListItem {
    // 헤더(섹션 타이틀)용
    data class HeaderItem(val sectionName: String) : RewardListItem()

    // 일반 알림 아이템용
    data class NormalItem(val notification: RewardItem) : RewardListItem()
}