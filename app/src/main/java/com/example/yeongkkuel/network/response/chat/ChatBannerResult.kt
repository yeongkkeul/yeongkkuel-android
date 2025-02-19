package com.example.yeongkkuel.network.response.chat

import com.google.gson.annotations.SerializedName

data class ChatBannerResult (
    @SerializedName("achievingCount") val achievingCount: Int,
    @SerializedName("chatRoomUserCount") val chatRoomUserCount: Int,
    @SerializedName("avgAmount") val avgAmount: Int,
    @SerializedName("age") val age: String,
    @SerializedName("job") val job: String,
    @SerializedName("topRate") val topRate: Int,
    @SerializedName("createdAt") val createdAt: String
)