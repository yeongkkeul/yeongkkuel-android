package com.example.yeongkkuel.presentation.my.reward

import com.example.yeongkkuel.network.response.mypage.RewardsResult
import com.example.yeongkkuel.presentation.my.reward.data.RewardItem
import com.example.yeongkkuel.presentation.my.reward.data.RewardType
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object RewardMapper {

    fun mapToRewardItem(rewardsResult: RewardsResult): RewardItem {
        val rewardType = convertStringToRewardType(rewardsResult.record)
        val message = rewardsResult.type
        val rewardText = rewardsResult.reward.toString()

        val section = convertDatetimeToSection(rewardsResult.datetime)

        return RewardItem(
        type = rewardType,
        message = message,
        rewardText = rewardText,
        section = section)
    }


}

private fun convertStringToRewardType(typeString: String): RewardType {
    return when (typeString) {
        "개인 목표 달성" -> RewardType.GOAL
        "팀 목표 달성" -> RewardType.TEAM_GOAL
        else -> RewardType.DEFAULT // default (혹은 예외처리)
    }
}

private fun convertDatetimeToSection(datetime: String): String {
    return try {
        // OffsetDateTime parse (예: 2025-02-11T08:25:06.858Z)
        val odt = OffsetDateTime.parse(datetime, DateTimeFormatter.ISO_DATE_TIME)
        val localDate = odt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDate()

        val today = LocalDate.now()
        val days = ChronoUnit.DAYS.between(localDate, today).toInt()

        when {
            days == 0 -> "오늘"
            days == 1 -> "어제"
            days in 2..6 -> "최근 7일"  // 2~6일 차이면 최근 7일로 표기
            else -> "이전"
        }
    } catch (e: Exception) {
        // 파싱 실패하면 기본 문구
        "최근"
    }
}