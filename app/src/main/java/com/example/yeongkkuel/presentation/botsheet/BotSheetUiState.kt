package com.example.yeongkkuel.presentation.botsheet

import com.example.yeongkkuel.R
import com.example.yeongkkuel.presentation.util.Colors
import com.example.yeongkkuel.presentation.util.SpendingCategory
import java.util.Date

data class BotSheetUiState(
    val total: Int = 300000,
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
            val name: String,
            val price: Int
        )
    }

    companion object {
        fun init() = BotSheetUiState(
            spendingList = emptyList(), // 초기 상태는 빈 리스트
            date = Date()
        )
    }
}