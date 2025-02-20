package com.example.yeongkkuel.network.response.chat

import com.google.gson.annotations.SerializedName

data class ChatRoomInfoResult(
    @SerializedName("chatRoomId") val chatRoomId: Int,
    @SerializedName("chatRoomTitle") val chatRoomTitle: String,
    @SerializedName("chatRoomThumbnail") val chatRoomThumbnail: String?,
    @SerializedName("chatRoomRule") val chatRoomRule: String,
    @SerializedName("participationCount") val participationCount: Int
)