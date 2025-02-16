package com.example.yeongkkuel.presentation.my.reward.data

import com.google.gson.annotations.SerializedName
import java.io.Serial

enum class RewardType {
    @SerializedName("JOIN_CHALLENGER_ROOM") CHALLENGE_JOIN,    // 챌린지 방 가입
    @SerializedName("AWARD_RANKING_REWARDS")  RANKING_REWARD,    // 랭킹 리워드
    @SerializedName("AWARD_NO_SPENDING_REWARDS")  NO_SPEND_REWARD,   // 무지출 리워드
    @SerializedName("EXCEED_DAILY_SPENDING_GOAL")  DAILY_EXCEED,      // 하루 지출 목표액 초과
    @SerializedName("UPDATE_CHALLENGE_GROUP_RANKING")  CHALLENGE_RANKING_UPDATE
}
