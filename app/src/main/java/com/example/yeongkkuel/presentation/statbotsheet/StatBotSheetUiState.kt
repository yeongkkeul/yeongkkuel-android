package com.example.yeongkkuel.presentation.statbotsheet

import androidx.annotation.ColorRes
import com.example.yeongkkuel.R

data class StatBotSheetUiState(
    val total: Int = 300000,
    val spendingList: List<Spending>
) {
    data class Spending(
        val kind: String,
        @ColorRes val color: Int,
        val history: List<History>
    ) {
        data class History(
            val name: String,
            val price: Int
        )
    }

    companion object {
        fun init() = StatBotSheetUiState(
            spendingList = listOf(
                Spending(
                    kind = "간식/음료1",
                    color = R.color.green,
                    history = listOf(
                        Spending.History("아아", 15000)
                    )
                ),
                Spending(
                    kind = "간식/음료2",
                    color = R.color.green,
                    history = listOf(
                        Spending.History("아아", 15000)
                    )
                ),Spending(
                    kind = "간식/음료3",
                    color = R.color.green,
                    history = listOf(
                        Spending.History("아아", 15000)
                    )
                ),Spending(
                    kind = "간식/음료4",
                    color = R.color.green,
                    history = listOf(
                        Spending.History("아아", 15000)
                    )
                ),
                Spending(
                    kind = "간식/음료5",
                    color = R.color.green,
                    history = listOf(
                        Spending.History("아아", 15000)
                    )
                ),
                Spending(
                    kind = "간식/음료",
                    color = R.color.green,
                    history = listOf(
                        Spending.History("아아", 15000)
                    )
                ),Spending(
                    kind = "간식/음료",
                    color = R.color.green,
                    history = listOf(
                        Spending.History("아아", 15000)
                    )
                ),Spending(
                    kind = "간식/음료",
                    color = R.color.green,
                    history = listOf(
                        Spending.History("아아", 15000)
                    )
                ),Spending(
                    kind = "간식/음료",
                    color = R.color.green,
                    history = listOf(
                        Spending.History("아아", 15000)
                    )
                ),Spending(
                    kind = "간식/음료",
                    color = R.color.green,
                    history = listOf(
                        Spending.History("아아", 15000)
                    )
                ),Spending(
                    kind = "간식/음료",
                    color = R.color.green,
                    history = listOf(
                        Spending.History("아아", 15000)
                    )
                ),







            )
        )
    }
}