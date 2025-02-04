package com.example.yeongkkuel.presentation.home.category.data

data class CategoryRequest(
    val name: String, // 카테고리 이름
    val color: String // 카테고리 색상 (String으로 서버에서 내려옴)
)
