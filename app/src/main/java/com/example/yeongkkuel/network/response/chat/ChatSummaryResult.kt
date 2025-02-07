package com.example.yeongkkuel.network.response.chat

import com.google.gson.annotations.SerializedName

data class ChatSummaryResult(
    @SerializedName("chatRoomTitle") val chatRoomTitle: String,
    @SerializedName("lastActivity") val lastActivity: String?, // 활동이 없을 경우 null 가능
    @SerializedName("chatRoomAgeRange") val chatRoomAgeRange: String,
    @SerializedName("chatRoomJob") val chatRoomJob: String,
    @SerializedName("createdDaysElapsed") val createdDaysElapsed: String,
    @SerializedName("participationCount") val participationCount: Int,
    @SerializedName("chatRoomMaxUserCount") val chatRoomMaxUserCount: Int,
    @SerializedName("chatRoomSpendingAmountGoal") val chatRoomSpendingAmountGoal: Int,
    @SerializedName("chatRoomImageUrl") val chatRoomImageUrl: String,
    @SerializedName("isPassword") val isPassword: Boolean
)