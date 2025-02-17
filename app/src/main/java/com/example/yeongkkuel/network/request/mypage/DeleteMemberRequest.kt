package com.example.yeongkkuel.network.request.mypage

import com.google.gson.annotations.SerializedName

data class DeleteMemberRequest(
    @SerializedName("reason") val reason: String,
    @SerializedName("detail") val detail: String?
)