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
import timber.log.Timber
import java.util.Calendar
import java.util.Date

class StatMonthlyViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<StatMonthlyUiState>(StatMonthlyUiState.Init)
    val uiState = _uiState.asStateFlow()

    private val yeongkkuelService = RetrofitClient.statService
    private val dayOfWeekList: List<StatMonthlyUiState.StatMonthly.CalendarData> =
        Week.getListItem().map { week ->
            StatMonthlyUiState.StatMonthly.CalendarData.CalendarDayOfWeek(week)
        }

    fun getCalender(year: Int, month: Int) = viewModelScope.launch {
        suspend fun List<StatMonthlyUiState.StatMonthly.CalendarData.CalendarDay>.getData(): List<StatMonthlyUiState.StatMonthly.CalendarData.CalendarDay> {
            try {
                yeongkkuelService.getExpendituresMonthCalendar(year = year, month = month).run {
                    if (isSuccess) {
                        _uiState.update {
                            StatMonthlyUiState.StatMonthly.init().copy(
                                targetExpenditure = result.dayTargetExpenditure,
                                achieveDay = result.achieveDays,
                                rewardsAmount = result.rewards,
                                totalSpending = result.totalMonthExpenditure
                            )
                        }

                        val dataList = result.selectedMonthExpenses.map { expense ->
                            val pieDataList =
                                result.dayTargetExpenditure?.let { targetExpenditure ->
                                    val rest = targetExpenditure - expense.expenditure
                                    mutableListOf<PieEntry>().apply {
                                        add(PieEntry(expense.expenditure.toFloat(), "지출"))
                                        if (rest > 0) {
                                            add(PieEntry(rest.toFloat(), "나머지"))
                                        }
                                    }
                                } ?: emptyList()

                            StatMonthlyUiState.StatMonthly.CalendarData.CalendarDay(
                                targetMonth = Pair(year, month),
                                day = expense.expenseDate.getDay(),
                                pieDataList = pieDataList
                            )
                        }

                        val mergedList = this@getData.toMutableList()

                        dataList.forEach { newData ->
                            val existingIndex = mergedList.indexOfFirst { it.day == newData.day }
                            if (existingIndex != -1) {
                                val existingDay = mergedList[existingIndex]
                                mergedList[existingIndex] = existingDay.copy(
                                    pieDataList = newData.pieDataList
                                )
                            } else {
                                mergedList.add(newData)
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
        ): List<StatMonthlyUiState.StatMonthly.CalendarData.CalendarDay> {
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

                StatMonthlyUiState.StatMonthly.CalendarData.CalendarDay(
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
            (prev as StatMonthlyUiState.StatMonthly).copy(
                targetMonth = Pair(year, month),
                calendarList = dayOfWeekList + dayList
            )
        }
    }

}