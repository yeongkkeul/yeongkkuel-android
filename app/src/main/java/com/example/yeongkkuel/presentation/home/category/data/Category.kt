package com.example.yeongkkuel.presentation.home.category.data

import com.example.yeongkkuel.presentation.util.Colors


data class Category(
    val id: Int,      // 카테고리 ID (서버와 동기화)
    val name: String,  // 카테고리 이름
    val color: Colors  // 카테고리 색상 (Color 리소스 ID)
)

// 서버 응답을 로컬 데이터로 변환
fun CategoryResponse.toCategory(): Category {
    return Category(
        id = this.id, // 서버 응답의 ID를 로컬 데이터에 매핑
        name = this.name,
        color = Colors.valueOf(this.color) // 서버에서 받은 색상을 Colors enum으로 변환
    )
}

// 로컬 데이터를 서버 요청 데이터로 변환
fun Category.toRequest(): CategoryRequest {
    return CategoryRequest(
        name = this.name,
        color = this.color.name // Colors enum의 이름(String)을 서버로 보냄
    )
}
