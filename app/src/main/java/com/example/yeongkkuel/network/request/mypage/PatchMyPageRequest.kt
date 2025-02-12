package com.example.yeongkkuel.network.request.mypage

import com.google.gson.annotations.SerializedName

data class PatchMyPageRequest(
    @SerializedName("nickname") val nickname: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("ageGroup") val ageGroup: String,
    @SerializedName("job") val job: String
)