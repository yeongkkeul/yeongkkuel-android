package com.example.yeongkkuel.presentation.util

enum class SpendingCategory(val kor: String) {
    SHOP("쇼핑"),
    IMPROVEMENT("자기계발"),
    SNACK("간식/음료"),
    BEAUTY("미용"),
    ETC("기타");

    companion object {
        fun fromKor(kor: String): SpendingCategory {
            return values().find { it.kor == kor } ?: ETC
        }
    }
}
