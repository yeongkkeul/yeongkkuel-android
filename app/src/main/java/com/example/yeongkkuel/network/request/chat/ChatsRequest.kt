package com.example.yeongkkuel.network.request.chat

import com.google.gson.annotations.SerializedName

data class ChatsRequest(
    @SerializedName("chatRoomTitle") val chatRoomTitle: String,
    @SerializedName("chatRoomPassword") val chatRoomPassword: String?, // 비밀번호가 없으면 null 허용
    @SerializedName("chatRoomSpendingAmountGoal") val chatRoomSpendingAmountGoal: Int,
    @SerializedName("chatRoomMaxUserCount") val chatRoomMaxUserCount: Int,
    @SerializedName("chatRoomAgeRange") val chatRoomAgeRange: String,
    @SerializedName("chatRoomJob") val chatRoomJob: String,
    @SerializedName("chatRoomRule") val chatRoomRule: String,
    @SerializedName("chatRoomImageUrl") val chatRoomImageUrl: String
)
