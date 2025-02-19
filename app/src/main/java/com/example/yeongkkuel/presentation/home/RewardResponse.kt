package com.example.yeongkkuel.presentation.home

data class RewardResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: RewardResult
)

data class RewardResult(
    val yesterdayReward: Int
)
