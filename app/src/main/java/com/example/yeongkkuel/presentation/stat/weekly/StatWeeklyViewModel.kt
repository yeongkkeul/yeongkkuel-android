package com.example.yeongkkuel.presentation.stat.weekly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yeongkkuel.network.RetrofitClient
import com.example.yeongkkuel.presentation.util.Age
import com.example.yeongkkuel.presentation.util.Colors
import com.example.yeongkkuel.presentation.util.Job
import com.example.yeongkkuel.presentation.util.SpendingCategory
import com.example.yeongkkuel.presentation.util.Week
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class StatWeeklyViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<StatWeeklyUiState>(StatWeeklyUiState.init())
    val uiState = _uiState.asStateFlow()

    private val yeongkkuelService = RetrofitClient.statService

    fun getWeekExpenditureList() = viewModelScope.launch {
        try {
            yeongkkuelService.getExpendituresWeekExpenses().run {
                if (isSuccess) {
                    result.run {
                        _uiState.update { prev ->
                            prev.copy(
                                weekList = expenses.map {
                                    val dayOfWeek = getDayOfWeek(it.expenseDate)
                                    StatWeeklyUiState.DayData(
                                        dayOfWeek,
                                        Entry(getYByDayOfWeek(dayOfWeek), it.expenditure?.toFloat() ?: 0.0f)
                                    )
                                },
                                totalSpending = weekExpenditure,
                                targetSpending = dayTargetExpenditure
                            )
                        }
                        Timber.d("result: ${_uiState.value}")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun getWeekExpenditureAverage() = viewModelScope.launch {
        try {
            yeongkkuelService.getExpendituresWeekAverage().run {
                if (isSuccess) {
                    result.run {
                        _uiState.update { prev ->
                            prev.copy(
                                compareList = listOf(
                                    StatWeeklyUiState.CompareData.OthersCompare(
                                        target = "${Age.getEnToKor(age)} ${Job.getEnToKor(job)}",
                                        targetSpending = averageExpenditure,
                                        mySpending = myAverageExpenditure,
                                        spendingUnit = SpendingUnit.WEEK,
                                        percentile = topPercent
                                    ),
                                    StatWeeklyUiState.CompareData.PastCompare(
                                        pastSpending = lastWeekExpenditure,
                                        currentSpending = thisWeekExpenditure,
                                        spendingUnit = SpendingUnit.WEEK
                                    )
                                ),
                                pieChartList = categories.map {
                                    StatWeeklyUiState.PieChartData(
                                        category = SpendingCategory.fromKor(it.categoryName),
                                        expenditure = it.totalExpenditure,
                                        color = Colors.getRGB(red= it.red, blue = it.blue, green = it.green)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



    private fun getDayOfWeek(date: String): Week {
        val parts = date.split(", ")
        val dayOfWeek = parts[1] // "Saturday" 추출
        return Week.fromString(dayOfWeek)
    }

    private fun getYByDayOfWeek(dayOfWeek:Week): Float{
       return when(dayOfWeek){
            Week.SUN -> 6.0f
            Week.MON -> 0.0f
            Week.TUE -> 1.0f
            Week.WED -> 2.0f
            Week.THU -> 3.0f
            Week.FRI -> 4.0f
            Week.SAT -> 5.0f
            Week.ERROR -> 0f
        }
    }
}