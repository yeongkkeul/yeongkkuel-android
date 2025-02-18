package com.example.yeongkkuel.presentation.stat.monthly

import com.example.yeongkkuel.presentation.util.Week
import com.github.mikephil.charting.data.PieEntry

sealed class StatMonthlyUiState {

    // Initial state
    data object Init : StatMonthlyUiState()

    // State with data
    data class StatMonthly(
        val targetMonth: Pair<Int, Int>, // year, month
        val totalSpending: Int,
        val rewardsAmount: Int,
        val achieveDay: Int?,
        val calendarList: List<CalendarData>,
        val targetExpenditure: Int?
    ) : StatMonthlyUiState() {

        // Sealed interface for calendar data
        sealed interface CalendarData {
            data class CalendarDayOfWeek(
                val dayOfWeek: Week
            ) : CalendarData

            data class CalendarDay(
                val targetMonth: Pair<Int, Int>, // year, month
                val day: Int,
                val pieDataList: List<PieEntry>
            ) : CalendarData
        }

        companion object {
            fun init() = StatMonthly(
                targetMonth = Pair(2025, 1),
                totalSpending = 0,
                rewardsAmount = 140,
                achieveDay = 13,
                targetExpenditure = null,
                calendarList = emptyList()
            )
        }
    }
}
