package com.example.yeongkkuel.network.response

import com.google.gson.annotations.SerializedName

data class Response<T>(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: T
)
