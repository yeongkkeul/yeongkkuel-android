package com.example.yeongkkuel.presentation.home.store

data class ProductUiState(
    val totalProducts: Int = 0, // ✅ 초기 상품 개수 0
    val productList: List<Product> = emptyList() // ✅ 빈 리스트로 초기화
) {
    data class Product(
        val id: Int,
        val name: String,
        val price: Int,
        val category: ProductCategory,
        val imageUrl: String // ✅ 로컬 리소스 ID 대신 서버 이미지 URL 사용
    )

    companion object {
        fun init() = ProductUiState() // ✅ 초기 빈 상태 반환
    }
}
