package com.example.yeongkkuel.presentation.botsheet

import com.example.yeongkkuel.presentation.util.Colors
import com.example.yeongkkuel.presentation.util.SpendingCategory
import java.util.Date

data class BotSheetUiState(
    val targetSpending: Int = 0,
    val spendingList: List<Spending>,
    val date: Date

) {
    data class Spending(
        val categoryId: Int,
        val kind: SpendingCategory,
        val color: Colors,
        val plusIconResId: Int,
        val history: List<History>
    ) {
        data class History(
            val id: Int,
            val name: String,       // 지출 이름
            val price: Int,         // 금액
            val imgExist: Boolean   // 사진 URL 추가
        )
    }

    companion object {
        fun init() = BotSheetUiState(
            spendingList = emptyList(), // 초기 상태는 빈 리스트
            date = Date()
        )
    }
}