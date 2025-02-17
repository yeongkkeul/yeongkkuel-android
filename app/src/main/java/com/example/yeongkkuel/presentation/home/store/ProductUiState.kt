package com.example.yeongkkuel.presentation.home.store

data class ProductUiState(
    val totalProducts: Int = 0, // ✅ 초기 상품 개수 0
    val productList: List<Product> = emptyList() // ✅ 빈 리스트로 초기화
) {
    data class Product(
        val id: Int,
        val name: String,
        val price: Int,
        val imageUrl: String,
        val category: ProductCategory,
        val area: String? = null,       // ✅ area 필드 (기존에 있다면 유지)
        val itemType: String            // ✅ itemType 필드 추가
    )

    companion object {
        fun init() = ProductUiState() // ✅ 초기 빈 상태 반환
    }
}
