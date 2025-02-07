package com.example.yeongkkuel.presentation.home.category.data

import com.google.gson.annotations.SerializedName

data class CategoryRequest(
    @SerializedName("categoryName") val name: String,  // 카테고리 이름
    val red: Int,
    val green: Int,
    val blue: Int
)