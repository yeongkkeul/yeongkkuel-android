package com.example.yeongkkuel.presentation.util

sealed class SpendingCategory(val kor: String) {
    object SNACK : SpendingCategory("간식/음료")
    object SHOP : SpendingCategory("밥/배달")
    object BEAUTY : SpendingCategory("화장품")
    object IMPROVEMENT : SpendingCategory("개선")
    data class CUSTOM(val name: String) : SpendingCategory(name)  // CUSTOM은 name을 동적으로 받는다.

    companion object {
        // fromKor 함수 추가
        fun fromKor(kor: String): SpendingCategory {
            return when (kor) {
                SNACK.kor -> SNACK
                SHOP.kor -> SHOP
                BEAUTY.kor -> BEAUTY
                IMPROVEMENT.kor -> IMPROVEMENT
                else -> CUSTOM(kor) // 등록되지 않은 경우 CUSTOM으로 처리
            }
        }
    }
}
