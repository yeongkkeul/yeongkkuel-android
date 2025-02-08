package com.example.yeongkkuel.network.response.chat

import com.google.gson.annotations.SerializedName

data class ReceiptResult(
    @SerializedName("senderName") val senderName: String,
    @SerializedName("category") val category: String,
    @SerializedName("content") val content: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("isNoSpending") val isNoSpending: Boolean
)
