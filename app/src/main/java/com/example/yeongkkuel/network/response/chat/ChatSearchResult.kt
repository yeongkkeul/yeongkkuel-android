package com.example.yeongkkuel.network.response.chat

import com.google.gson.annotations.SerializedName

data class ChatSearchResult(
    @SerializedName("existsChatRoom") val existsChatRoom: Boolean,
    @SerializedName("chatRooms") val chatRooms: List<ChatRoom>
) {
    data class ChatRoom(
        @SerializedName("chatRoomId") val chatRoomId: String,
        @SerializedName("chatRoomTitle") val chatRoomTitle: String,
        @SerializedName("createdAt") val createdAt: String?,
        @SerializedName("updatedAt") val updatedAt: String?,
        @SerializedName("chatRoomAgeRange") val chatRoomAgeRange: String,
        @SerializedName("chatRoomMaxUserCount") val chatRoomMaxUserCount: Int,
        @SerializedName("chatRoomThumbnail") val chatRoomThumbnail: String,
        @SerializedName("chatRoomJob") val chatRoomJob: String,
        @SerializedName("chatRoomDDay") val chatRoomDDay: Int,
        @SerializedName("chatRoomSpendingAmount") val chatRoomSpendingAmount: Int
    )
}