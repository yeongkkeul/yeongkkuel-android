package com.example.yeongkkuel.presentation.stat.weekly

import com.example.yeongkkuel.presentation.util.Colors
import com.example.yeongkkuel.presentation.util.SpendingCategory
import com.example.yeongkkuel.presentation.util.Week
import com.github.mikephil.charting.data.Entry

data class StatWeeklyUiState(
    val targetSpending: Int,
    val totalSpending: Int,
    val weekList: List<DayData>,
    val compareList: List<CompareData>,
    val pieChartList: List<PieChartData>
) {
    data class DayData(
        val dayOfWeek: Week,
        val entry: Entry?
    )

    sealed interface CompareData {
        data class OthersCompare(
            val target: String,
            val targetSpending: Int,
            val mySpending: Int, // 주간 지출에서 평균 구해서 넣기
            val spendingUnit: SpendingUnit = SpendingUnit.WEEK,
            val percentile: Int,
        ) : CompareData

        data class PastCompare(
            val pastSpending: Int,
            val currentSpending: Int,
            val spendingUnit: SpendingUnit = SpendingUnit.WEEK
        ) : CompareData
    }

    data class PieChartData(
        val category: SpendingCategory,
        val expenditure: Int,
        val color: Colors
    )

    companion object {
        fun init() = StatWeeklyUiState(
            targetSpending = 10000,
            weekList = listOf(
                DayData(Week.MON, Entry(0f, 2000f)),
                DayData(Week.TUE, Entry(1f, 1500f)),
                DayData(Week.WED, Entry(2f, 30000f)),
                DayData(Week.THU, Entry(3f, 500f)),
                DayData(Week.FRI, Entry(4f, 25000f)),
                DayData(Week.SAT, null),
                DayData(Week.SUN, null),
            ),
            compareList = listOf(
                CompareData.OthersCompare(
                    target = "20대 직장인",
                    targetSpending = 15700,
                    mySpending = 8100,
                    spendingUnit = SpendingUnit.DAY,
                    percentile = 10
                ),
                CompareData.PastCompare(
                    pastSpending = 122038,
                    currentSpending = 100383,
                    spendingUnit = SpendingUnit.DAY
                )
            ),
            pieChartList = listOf(
                PieChartData(
                    category = SpendingCategory.SNACK,
                    expenditure = 15800,
                    Colors.GREEN
                ),
                PieChartData(
                    category = SpendingCategory.SHOP,
                    expenditure = 158000,
                    Colors.PINK
                ),
                PieChartData(
                    category = SpendingCategory.ETC,
                    expenditure = 158000,
                    Colors.BLUE
                ),
            ),
            totalSpending = 56700
        )
    }
}


enum class SpendingUnit(val kor: String) {
    DAY("/일"), WEEK("/주"), MONTH("/월")
}