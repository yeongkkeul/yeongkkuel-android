package com.example.yeongkkuel.presentation.util

sealed class SpendingCategory(val kor: String) {
    object SNACK : SpendingCategory("간식/음료")
    object SHOP : SpendingCategory("밥/배달")
    object BEAUTY : SpendingCategory("화장품")
    object IMPROVEMENT : SpendingCategory("개선")
    data class CUSTOM(val name: String) : SpendingCategory(name)  // CUSTOM은 name을 동적으로 받는다.
}