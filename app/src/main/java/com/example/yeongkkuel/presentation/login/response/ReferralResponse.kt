package com.example.yeongkkuel.presentation.login.response

data class ReferralResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: Any
)
