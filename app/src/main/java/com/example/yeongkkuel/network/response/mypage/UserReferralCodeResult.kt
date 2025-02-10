package com.example.yeongkkuel.network.response.mypage

import com.google.gson.annotations.SerializedName

data class UserReferralCodeResult(
    @SerializedName("userReferralCode") val userReferralCode: String
)