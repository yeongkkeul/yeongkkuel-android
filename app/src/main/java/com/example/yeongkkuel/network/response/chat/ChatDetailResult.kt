package com.example.yeongkkuel.network.response.chat

import com.google.gson.annotations.SerializedName

data class ChatDetailResult (
    @SerializedName("chatRoomTitle") val chatRoomTitle: String,
    @SerializedName("lastActivity") val lastActivity: String,
    @SerializedName("chatRoomAgeRange") val chatRoomAgeRange: String,
    @SerializedName("chatRoomJob") val chatRoomJob: String,
    @SerializedName("createdDaysElapsed") val createdDaysElapsed: String,
    @SerializedName("chatRoomChallenger") val chatRoomChallenger: String,
    @SerializedName("chatRoomSpendingAmountGoal") val chatRoomSpendingAmountGoal: String,
    @SerializedName("chatRoomAchievedCount") val chatRoomAchievedCount: String,
    @SerializedName("chatRoomAverageExpense") val chatRoomAverageExpense: String,
    @SerializedName("chatRoomChallengerGroupRanking") val chatRoomChallengerGroupRanking: String,
    @SerializedName("chatRoomImageUrl") val chatRoomImageUrl: String,
    @SerializedName("isPassword") val isPassword: Boolean
)