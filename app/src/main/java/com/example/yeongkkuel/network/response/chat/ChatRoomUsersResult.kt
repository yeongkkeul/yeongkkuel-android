package com.example.yeongkkuel.network.response.chat

import com.google.gson.annotations.SerializedName

data class ChatRoomUsersResponse(
    @SerializedName("userInfos")
    val userInfos: List<UserInfo>
)

data class UserInfo(
    @SerializedName("userId")
    val userId: Int,
    @SerializedName("userName")
    val userName: String
)
