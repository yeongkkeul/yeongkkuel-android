package com.example.yeongkkuel.network.response.chat

import com.google.gson.annotations.SerializedName

data class ChatSearchResult(
    @SerializedName("publicChatRoomDetailDtos") val publicChatRoomDetailDtos: List<PublicChatRoomDetailDto>
) {
    data class PublicChatRoomDetailDto(
        @SerializedName("chatRoomId") val chatRoomId: Int,
        @SerializedName("chatRoomTitle") val chatRoomTitle: String,
        @SerializedName("chatRoomAgeRange") val chatRoomAgeRange: String,
        @SerializedName("chatRoomJob") val chatRoomJob: String,
        @SerializedName("chatRoomMaxUserCount") val chatRoomMaxUserCount: Int,
        @SerializedName("chatRoomParticipationCount") val chatRoomParticipationCount: Int,
        @SerializedName("chatRoomThumbnail") val chatRoomThumbnail: String,
        @SerializedName("chatRoomDDay") val chatRoomDDay: String,
        @SerializedName("chatRoomSpendingAmount") val chatRoomSpendingAmount: Int,
        @SerializedName("createdAt") val createdAt: String,
        @SerializedName("updatedAt") val updatedAt: String,
        @SerializedName("isPassword") val isPassword: Boolean
    )
}

