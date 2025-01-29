package com.example.yeongkkuel.presentation.botsheet

import com.example.yeongkkuel.R
import com.example.yeongkkuel.presentation.util.Colors
import com.example.yeongkkuel.presentation.util.SpendingCategory
import java.util.Date

data class BotSheetUiState(
    val targetSpending: Int = 300000,
    val spendingList: List<Spending>,
    val date: Date

) {
    data class Spending(
        val kind: SpendingCategory,
        val color: Colors,
        val plusIconResId: Int,
        val history: List<History>
    ) {
        data class History(
            val name: String,       // 🔹 지출 이름
            val price: Int,         // 🔹 금액
            val date: String,       // 🔹 지출 날짜 추가
            val categoryName: String,
            val categoryColor: String,
            val content: String,    // 🔹 지출 내용 추가
            val photoUrl: String    // 🔹 사진 URL 추가
        )
    }

    companion object {
        fun init() = BotSheetUiState(
            spendingList = emptyList(), // 초기 상태는 빈 리스트
            date = Date()
        )
    }
}