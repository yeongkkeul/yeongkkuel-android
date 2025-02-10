package com.example.yeongkkuel.presentation.util

sealed class SpendingCategory(val name: String) {
    // 동적으로 카테고리 생성
    data class CUSTOM(val customName: String) : SpendingCategory(customName)

    companion object {
        // 이름으로부터 SpendingCategory 반환
        fun fromName(name: String): SpendingCategory {
            return CUSTOM(name) // 모든 카테고리를 동적으로 생성
        }
    }
}
