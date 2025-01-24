package com.example.yeongkkuel.presentation.stat.monthly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yeongkkuel.presentation.network.RetrofitClient
import com.example.yeongkkuel.presentation.util.Week
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class StatMonthlyViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(StatMonthlyUiState.init())
    val uiState = _uiState.asStateFlow()

    private val yeongkkuelService = RetrofitClient.yeongkkuelService

    fun getCalender(year: Int, month:Int) = viewModelScope.launch {
        fun getDayList(year: Int, month: Int): List<StatMonthlyUiState.CalendarData.CalendarDay> {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, 1)
            }

            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val firstDayOfWeek =
                calendar.get(Calendar.DAY_OF_WEEK) // 1일의 요일 가져오기 (1: 일요일, 2: 월요일, ..., 7: 토요일)

            val daysList = mutableListOf<Int>()

            for (i in 1 until firstDayOfWeek - 1) {
                daysList.add(0)
            }

            // 해당 월의 날짜 추가
            for (day in 1..daysInMonth) {
                daysList.add(day)
            }

            val targetSpending = 20000

            val resultList = daysList.map { day ->
                val daySpending = 1700

                val rest = if (targetSpending - daySpending > 0) targetSpending - daySpending else 0

                StatMonthlyUiState.CalendarData.CalendarDay(
                    day = day,
                    pieDataList = listOf(
                        PieEntry(daySpending.toFloat()), // 일일 지출
                        PieEntry(rest.toFloat())         // 남은 지출
                    )
                )
            }

            return resultList
        }

        val dayOfWeekList: List<StatMonthlyUiState.CalendarData> =
            Week.getListItem().map { week ->
                StatMonthlyUiState.CalendarData.CalendarDayOfWeek(week)
            }

        val dayList = getDayList(year,month)

        _uiState.update { prev->
            prev.copy(
                targetMonth = Pair(year, month),
                calendarList = dayOfWeekList + dayList
            )
        }
    }

}