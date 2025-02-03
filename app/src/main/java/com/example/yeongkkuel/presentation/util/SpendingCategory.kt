package com.example.yeongkkuel.presentation.util

sealed class SpendingCategory(val kor: String) {
    object SNACK : SpendingCategory("간식/음료")
    object SHOP : SpendingCategory("밥/배달")
    object BEAUTY : SpendingCategory("화장품")
    object IMPROVEMENT : SpendingCategory("개선")
    data class CUSTOM(val name: String) : SpendingCategory(name) // CUSTOM은 name을 동적으로 받음

    companion object {
        // kor로부터 SpendingCategory 객체를 반환
        fun fromKor(kor: String): SpendingCategory {
            return when (kor) {
                SNACK.kor -> SNACK
                SHOP.kor -> SHOP
                BEAUTY.kor -> BEAUTY
                IMPROVEMENT.kor -> IMPROVEMENT
                else -> CUSTOM(kor) // 등록되지 않은 경우 CUSTOM 처리
            }
        }

        // name (영문 이름)을 기반으로 SpendingCategory 객체 반환
        fun fromName(name: String): SpendingCategory {
            return when (name.uppercase()) {
                "SNACK" -> SNACK
                "SHOP" -> SHOP
                "BEAUTY" -> BEAUTY
                "IMPROVEMENT" -> IMPROVEMENT
                else -> CUSTOM(name) // 등록되지 않은 경우 CUSTOM 처리
            }
        }
    }
}
