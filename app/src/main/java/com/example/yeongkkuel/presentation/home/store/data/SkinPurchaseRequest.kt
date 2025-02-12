package com.example.yeongkkuel.presentation.home.store.data

data class SkinPurchaseRequest(
    val itemId: Int,
    val itemType: String,
    val itemName: String,
    val reward: Int
)

data class SkinPurchaseResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: String
)
