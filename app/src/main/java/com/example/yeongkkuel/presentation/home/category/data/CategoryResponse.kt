package com.example.yeongkkuel.presentation.home.category.data

import com.google.gson.annotations.SerializedName

data class CategoryResponse(
    @SerializedName("categoryId") val id: Int,
    @SerializedName("categoryName") val name: String,
    @SerializedName("red") val red: Int,               // RGB 값
    @SerializedName("green") val green: Int,           // RGB 값
    @SerializedName("blue") val blue: Int,             // RGB 값
)

data class CategoryListResponse(
    val categoryList: List<CategoryResponse>, // 카테고리 배열
    val totalElements: Int // 추가 정보
)