package com.example.yeongkkuel.presentation.home.category.data

data class CategoryResponse(
    val id: Int,     // 카테고리 ID (서버에서 사용)
    val name: String, // 카테고리 이름
    val color: String // 카테고리 색상 (String으로 서버에서 내려옴)
)

data class CategoryListResponse(
    val categoryList: List<CategoryResponse>, // 카테고리 배열
    val totalElements: Int // 추가 정보
)