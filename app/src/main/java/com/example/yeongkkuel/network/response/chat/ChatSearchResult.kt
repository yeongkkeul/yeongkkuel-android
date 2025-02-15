package com.example.yeongkkuel.network.response.chat

import com.google.gson.annotations.SerializedName

data class ChatSearchResult(
    @SerializedName("publicChatRoomDetailDtos")
    val publicChatRoomDetailDtos: List<ChatRoomDetailDto>
)

data class ChatRoomDetailDto(
    val chatRoomId: Int,
    val chatRoomTitle: String,
    val chatRoomAgeRange: String,
    val chatRoomJob: String,
    val chatRoomMaxUserCount: String,
    val chatRoomParticipationCount: Int,
    val chatRoomThumbnail: String?, // null 가능하므로 nullable 처리
    val chatRoomDDay: String,
    val chatRoomSpendingAmount: String,
    val createdAt: String, // Date 타입으로 파싱하고 싶다면 변환 고려
    val updatedAt: String,
    val isPassword: Boolean
)
