package com.example.yeongkkuel.presentation.home.store.data

data class SkinEquipRequest(
    val userItem: List<SkinPurchase>
)

data class SkinPurchase(
    val purchaseId: Int
)
