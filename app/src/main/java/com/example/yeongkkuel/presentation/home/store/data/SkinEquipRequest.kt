package com.example.yeongkkuel.presentation.home.store.data

data class SkinEquipRequest(
    val userItem: List<SkinEquipItem>
)

data class SkinEquipItem(
    val purchaseId: Int
)
