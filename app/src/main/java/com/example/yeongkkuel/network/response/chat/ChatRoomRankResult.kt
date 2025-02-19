package com.example.yeongkkuel.network.response.chat

data class ChatRoomRankResponse(
    val userRanks: List<ChatRoomRank>
)

// 개별 유저 랭킹 모델
data class ChatRoomRank(
    val userId: Int,
    val nickname: String,
    val profileImage: String?,
    val rankScore: Double,
    val rank: Int
)
