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
        id = this.id,
        name = this.name,
        color = Colors.fromRGB(this.red, this.green, this.blue) ?: Colors.RED1 // ✅ RGB 값을 기반으로 Colors 변환
    )
}

// 로컬 데이터를 서버 요청 데이터로 변환
fun Category.toRequest(): CategoryRequest {
    return CategoryRequest(
        name = this.name,
        red = this.color.red,
        green = this.color.green,
        blue = this.color.blue
    )
}
