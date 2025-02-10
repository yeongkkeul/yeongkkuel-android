package com.example.yeongkkuel.presentation.stat.monthly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yeongkkuel.network.RetrofitClient
import com.example.yeongkkuel.presentation.util.Week
import com.example.yeongkkuel.presentation.util.getDay
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class StatMonthlyViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(StatMonthlyUiState.init())
    val uiState = _uiState.asStateFlow()

    private val yeongkkuelService = RetrofitClient.statService
    private val dayOfWeekList: List<StatMonthlyUiState.CalendarData> =
        Week.getListItem().map { week ->
            StatMonthlyUiState.CalendarData.CalendarDayOfWeek(week)
        }

    init {
        val date = Date()
        val year = date.year + 1900  // 현재 연도
        val month = date.month + 1   // 현재 월 (0부터 시작하므로 +1)

        getCalender(year = year, month = month)
    }

    fun getCalender(year: Int, month: Int) = viewModelScope.launch {
        suspend fun List<StatMonthlyUiState.CalendarData.CalendarDay>.getData(): List<StatMonthlyUiState.CalendarData.CalendarDay> {
            try {
                yeongkkuelService.getExpendituresMonthCalendar(year = year, month = month).run {
                    if (isSuccess) {
                        _uiState.update { prev->
                            prev.copy(
                                achieveDay = result.achievedDays,
                                rewardsAmount = result.rewards
                            )
                        }

                        val dataList = result.selectedMonthExpenses.map {
                            StatMonthlyUiState.CalendarData.CalendarDay(
                                targetExpenditure = result.dayTargetExpenditure,
                                targetMonth = Pair(year, month),
                                day = it.expenseDate.getDay(),
                                pieDataList = listOf(
                                    PieEntry(
                                        maxOf(
                                            (result.dayTargetExpenditure - it.expenditure).toFloat(),
                                            0f
                                        ), "나머지"
                                    ),
                                    PieEntry(it.expenditure.toFloat(), "지출")
                                )
                            )
                        }
                        val mergedList = this@getData.toMutableList()
                        dataList.forEach { data ->
                            val existingIndex = mergedList.indexOfFirst { it.day == data.day }
                            if (existingIndex != -1) {
                                val existingDay = mergedList[existingIndex]
                                mergedList[existingIndex] = existingDay.copy(
                                    pieDataList = existingDay.pieDataList + data.pieDataList
                                )
                            } else {
                                mergedList.add(data)
                            }
                        }
                        return mergedList
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return this
        }


        suspend fun getDayList(
            year: Int,
            month: Int
        ): List<StatMonthlyUiState.CalendarData.CalendarDay> {
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
                val daySpending = 0

                val rest = if (targetSpending - daySpending > 0) targetSpending - daySpending else 0

                StatMonthlyUiState.CalendarData.CalendarDay(
                    targetExpenditure = null,
                    targetMonth = Pair(year, month),
                    day = day,
                    pieDataList = listOf(
                        PieEntry(daySpending.toFloat()), // 일일 지출
                        PieEntry(rest.toFloat())         // 남은 지출
                    )
                )
            }
            return resultList.getData()
        }

        val dayList = getDayList(year, month)

        _uiState.update { prev ->
            prev.copy(
                targetMonth = Pair(year, month),
                calendarList = dayOfWeekList + dayList
            )
        }
    }

}