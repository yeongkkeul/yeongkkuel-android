package com.example.yeongkkuel.presentation.home.store.data

data class ShopResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: ShopResult
)

data class ShopResult(
    val myReward: Int, // 사용자의 보유 리워드
    val mySkin: List<MySkin>, // 사용자가 보유한 스킨 리스트
    val itemType: String, // 현재 조회하는 아이템 타입
    val itemList: List<ShopItem> // 상점에 있는 아이템 리스트
)

data class MySkin(
    val itemName: String,
    val itemType: String,
    val imgUrl: String
)

data class ShopItem(
    val id: Int,
    val itemName: String,
    val price: Int,
    val itemType: String?,
    val itemImg: String
)
