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
            spendingList = listOf(
                Spending(
                    kind = SpendingCategory.SNACK,
                    color = Colors.PINK,
                    plusIconResId = R.drawable.ic_plus_pink,
                    history = listOf(
                        Spending.History("아아", 16000)
                    )
                ),
                Spending(
                    kind = SpendingCategory.SHOP,
                    color = Colors.BLUE,
                    plusIconResId = R.drawable.ic_plus_blue,
                    history = listOf(
                        Spending.History("아아", 15000)
                    )
                ),Spending(
                    kind = SpendingCategory.BEAUTY,
                    color = Colors.GREEN,
                    plusIconResId = R.drawable.ic_plus_green,
                    history = listOf(
                        Spending.History("아아", 17000)
                    )
                ),Spending(
                    kind = SpendingCategory.ETC,
                    color = Colors.GREEN,
                    plusIconResId = R.drawable.ic_plus_green,
                    history = listOf(
                        Spending.History("아아", 15000)
                    )
                ),
                Spending(
                    kind = SpendingCategory.IMPROVEMENT,
                    color = Colors.GREEN,
                    plusIconResId = R.drawable.ic_plus_green,
                    history = listOf(
                        Spending.History("아아", 15000)
                    )
                )
            ),
            date = Date()
        )
    }
}