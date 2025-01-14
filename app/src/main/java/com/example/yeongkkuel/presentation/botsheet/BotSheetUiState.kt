package com.example.yeongkkuel.presentation.botsheet

import androidx.annotation.ColorRes
import com.example.yeongkkuel.R
import com.example.yeongkkuel.presentation.util.Colors

data class BotSheetUiState(
    val total: Int = 300000,
    val spendingList: List<Spending>
) {
    data class Spending(
        val kind: String,
        val color: Colors,
        val history: List<History>
    ) {
        data class History(
            val name: String,
            val price: Int
        )
    }

    companion object {
        fun init() = BotSheetUiState(
            spendingList = listOf(
                Spending(
                    kind = "간식/음료1",
                    color = Colors.PINK,
                    history = listOf(
                        Spending.History("아아", 15000)
                    )
                ),
                Spending(
                    kind = "간식/음료2",
                    color = Colors.BLUE,
                    history = listOf(
                        Spending.History("아아", 15000)
                    )
                ),Spending(
                    kind = "간식/음료3",
                    color = Colors.GREEN,
                    history = listOf(
                        Spending.History("아아", 15000)
                    )
                ),Spending(
                    kind = "간식/음료4",
                    color = Colors.GREEN,
                    history = listOf(
                        Spending.History("아아", 15000)
                    )
                ),
                Spending(
                    kind = "간식/음료5",
                    color = Colors.GREEN,
                    history = listOf(
                        Spending.History("아아", 15000)
                    )
                ),
                Spending(
                    kind = "간식/음료",
                    color = Colors.GREEN,
                    history = listOf(
                        Spending.History("아아", 15000)
                    )
                ),Spending(
                    kind = "간식/음료",
                    color = Colors.GREEN,
                    history = listOf(
                        Spending.History("아아", 15000)
                    )
                ),Spending(
                    kind = "간식/음료",
                    color = Colors.GREEN,
                    history = listOf(
                        Spending.History("아아", 15000)
                    )
                ),Spending(
                    kind = "간식/음료",
                    color = Colors.GREEN,
                    history = listOf(
                        Spending.History("아아", 15000)
                    )
                ),Spending(
                    kind = "간식/음료",
                    color = Colors.GREEN,
                    history = listOf(
                        Spending.History("아아", 15000)
                    )
                ),Spending(
                    kind = "간식/음료",
                    color = Colors.GREEN,
                    history = listOf(
                        Spending.History("아아", 15000)
                    )
                ),

            )
        )
    }
}