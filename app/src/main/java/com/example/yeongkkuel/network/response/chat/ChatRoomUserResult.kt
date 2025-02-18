package com.example.yeongkkuel.network.response.chat

data class ChatRoomUserResult(
    val userId: Int,
    val nickname: String,
    val profileImage: String?,
    val createdAt: String,
    val age: String,
    val job: String,
    val rank: Int
)
