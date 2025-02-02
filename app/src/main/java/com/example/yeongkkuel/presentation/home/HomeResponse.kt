package com.example.yeongkkuel.presentation.home

data class HomeResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: HomeResult
)

data class HomeResult(
    val myReward: Int, // 보유 리워드
    val mySkin: List<MySkin>, // 보유한 스킨 리스트
    val today: String, // 오늘 날짜
    val categories: List<Category> // 카테고리별 지출 내역
)

data class MySkin(
    val itemName: String,
    val itemType: String,
    val imgUrl: String
)

data class Category(
    val categoryName: String,
    val expenses: List<Expense>
)

data class Expense(
    val expenseId: Int,
    val content: String,
    val amount: Int
)
