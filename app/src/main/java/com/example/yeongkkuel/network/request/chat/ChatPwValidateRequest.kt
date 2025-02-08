package com.example.yeongkkuel.network.request.chat

import com.google.gson.annotations.SerializedName

data class ChatPwValidateRequest(
    @SerializedName("password") val password: String
)