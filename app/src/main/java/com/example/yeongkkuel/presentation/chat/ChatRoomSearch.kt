package com.example.yeongkkuel.presentation.chat

data class ChatRoomSearch(
    val chatRoomId: String,
    val chatRoomName: String,
    val chatRoomAgeRange: String,
    val chatRoomMaxUserCount: String,
    val chatRoomThumbnail: String,
    val chatRoomJob: String,
    val chatRoomDDay: Int,
    val chatRoomSpendingAmount: Int
)