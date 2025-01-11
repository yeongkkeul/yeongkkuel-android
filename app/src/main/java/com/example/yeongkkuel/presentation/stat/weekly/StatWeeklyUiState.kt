package com.example.yeongkkuel.presentation.stat.weekly

import com.github.mikephil.charting.data.Entry

data class StatWeeklyUiState(
    val targetSpending: Int,
    val weekList: List<DayData>
) {
    data class DayData(
        val dayOfWeek: Week,
        val entry: Entry?
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
            )
        )
    }
}

enum class Week(val kor: String) {
    SUN("일"), MON("월"), TUE("화"), WED("수"), THU("목"), FRI("금"), SAT("토")
}
