package com.example.yeongkkuel.presentation.home.store

import com.example.yeongkkuel.R

data class ProductUiState(
    val totalProducts: Int = 8, // 총 상품 개수
    val productList: List<Product>
) {
    data class Product(
        val id: Int, // ✅ id 추가
        val name: String,
        val price: Int,
        val category: ProductCategory, // ProductCategory 타입
        val iconResId: Int
    )

    companion object {
        fun init() = ProductUiState(
            productList = listOf(
                Product(
                    id =1,
                    name = "스케이트 보드",
                    price = 128,
                    category = ProductCategory.TOY,
                    iconResId = R.drawable.img_home_toy2,
                ),
                Product(
                    id =2,
                    name = "탱탱볼",
                    price = 128,
                    category = ProductCategory.TOY,
                    iconResId = R.drawable.img_home_toy1,
                ),
                Product(
                    id=3,
                    name = "밥그릇 1",
                    price = 64,
                    category = ProductCategory.BOWL,
                    iconResId = R.drawable.img_home_bowl1,
                ),
                Product(
                    id=4,
                    name = "밥그릇 2",
                    price = 64,
                    category = ProductCategory.BOWL,
                    iconResId = R.drawable.img_home_bowl2,
                ),
                Product(
                    id=5,
                    name = "둥지 1",
                    price = 256,
                    category = ProductCategory.NEST,
                    iconResId = R.drawable.img_home_nest1
                ),
                Product(
                    id=6,
                    name = "둥지 2",
                    price = 768,
                    category = ProductCategory.NEST,
                    iconResId = R.drawable.img_home_nest2,

                ),
                Product(
                    id=7,
                    name = "그네 1",
                    price = 256,
                    category = ProductCategory.SWING,
                    iconResId = R.drawable.img_home_swing1
                ),
                Product(
                    id=8,
                    name = "그네 2",
                    price = 768,
                    category = ProductCategory.SWING,
                    iconResId = R.drawable.img_home_swing2,

                    )

            )
        )
    }
}