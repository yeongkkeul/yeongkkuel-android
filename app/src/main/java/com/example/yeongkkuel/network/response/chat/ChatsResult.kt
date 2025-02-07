package com.example.yeongkkuel.network.response.chat

import com.google.gson.annotations.SerializedName

data class ChatsResult(
    @SerializedName("chatRooms") val chatRooms: List<ChatRoom>
) {
    data class ChatRoom(
        @SerializedName("chatRoomId") val chatRoomId: String,
        @SerializedName("chatName") val chatName: String,
        @SerializedName("chatUserCount") val chatUserCount: Int,
        @SerializedName("chatUpdatedAt") val chatUpdatedAt: String,
        @SerializedName("recentChatHistory") val recentChatHistory: String,
        @SerializedName("unreadChatCount") val unreadChatCount: Int,
        @SerializedName("chatRoomThumbnail") val chatRoomThumbnail: String
    )
}