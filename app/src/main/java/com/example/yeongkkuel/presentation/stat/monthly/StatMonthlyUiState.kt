package com.example.yeongkkuel.presentation.stat.monthly

import com.example.yeongkkuel.presentation.util.Week
import com.github.mikephil.charting.data.PieEntry

data class StatMonthlyUiState(
    val targetMonth: Pair<Int, Int>,// year, month
    val totalSpending: Int,
    val rewardsAmount: Int,
    val achieveDay: Int,
    val calendarList: List<CalendarData>,
) {
    sealed interface CalendarData {
        data class CalendarDayOfWeek(
            val dayOfWeek: Week
        ) : CalendarData

        data class CalendarDay(
            val day: Int,
            val pieDataList: List<PieEntry>
        ) : CalendarData
    }


    companion object {
        fun init() = StatMonthlyUiState(
            targetMonth = Pair(2025, 1),
            totalSpending = 210300,
            rewardsAmount = 140,
            achieveDay = 13,
            calendarList = emptyList()
        )
    }
}