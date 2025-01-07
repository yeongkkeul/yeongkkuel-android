package com.example.yeongkkuel.presentation.stat.daily

import com.github.mikephil.charting.data.PieEntry

data class StatDailyUiState(
    val spendingList: SpendingList
) {
    data class SpendingList(
        val total: Int = 300000,
        val snackList: List<Spending> = emptyList(),
        val selfImprovementList: List<Spending> = emptyList(),
        val beautyList: List<Spending> = emptyList()
    ) {
        data class Spending(
            val kind: String,
            val price: Int
        )
    }

    companion object {
        fun init() = StatDailyUiState(
            spendingList = SpendingList(
                snackList = listOf(
                    SpendingList.Spending("과자", 15000),
                    SpendingList.Spending("과자", 15000),
                    SpendingList.Spending("과자", 15000),
                    )
            )
        )
    }
}