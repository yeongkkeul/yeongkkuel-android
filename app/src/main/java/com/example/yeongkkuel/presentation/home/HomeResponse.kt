package com.example.yeongkkuel.presentation.home

import com.example.yeongkkuel.presentation.home.category.CategoryViewModel
import com.example.yeongkkuel.presentation.util.Colors
import com.example.yeongkkuel.presentation.home.category.data.Category // 기존 Category 클래스 사용
//import com.example.yeongkkuel.presentation.home.store.data.MySkin

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
    val categories: List<CategoryResponse> // 🚨 기존 List<Category> → List<CategoryResponse> 로 변경!
)

data class CategoryResponse(
    val categoryId: Int,
    val categoryName: String,
    val expenses: List<Expense>
)
data class MySkin(
    val itemName: String,
    val itemType: String,
    val imgUrl: String
)

data class Category(
    val categoryId: Int, // 카테고리 ID 추가
    val categoryName: String,
    val expenses: List<Expense>
)

data class Expense(
    val expenseId: Int,
    val content: String,
    val amount: Int,
    val imgExist: Boolean
)

fun CategoryResponse.toCategory(categoryViewModel: CategoryViewModel): Category {
    val existingCategory = categoryViewModel.categories.value?.find { it.id == this.categoryId }
    return Category(
        id = this.categoryId,
        name = this.categoryName,
        color = existingCategory?.color ?: Colors.RED1 // ✅ 기존 카테고리 색상 활용, 없으면 RED1
    )
}


