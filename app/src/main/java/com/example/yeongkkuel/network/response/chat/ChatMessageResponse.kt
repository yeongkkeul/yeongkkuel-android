package com.example.yeongkkuel.network.response.chat

import com.google.gson.annotations.SerializedName

data class ChatMessageResponse(
    @SerializedName("id") val id: String,
    @SerializedName("chatRoomId") val chatRoomId: Int,
    @SerializedName("senderId") val senderId: Int,
    @SerializedName("messageType") val messageType: String,
    @SerializedName("content") val content: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("unreadCount") val unreadCount: Int
)