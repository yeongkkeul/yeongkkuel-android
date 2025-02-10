package com.example.yeongkkuel.network.response.mypage

import com.google.gson.annotations.SerializedName

data class RewardsResult(
    @SerializedName("datetime") val datetime: String,
    @SerializedName("record") val record: String,
    @SerializedName("type") val type: String,
    @SerializedName("reward") val reward: Int
)