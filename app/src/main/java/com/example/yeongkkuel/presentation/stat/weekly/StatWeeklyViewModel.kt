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
                                    StatWeeklyUiState.DayData(
                                        getDayOfWeek(date = it.expenseDate),
                                        Entry(0f, it.expenditure?.toFloat() ?: 0.0f)
                                    )
                                },
                                totalSpending = weekExpenditure,
                                targetSpending = dayTargetExpenditure
                            )
                        }
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
}