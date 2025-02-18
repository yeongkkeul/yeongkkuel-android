package com.example.yeongkkuel.presentation.stat.weekly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yeongkkuel.network.RetrofitClient
import com.example.yeongkkuel.presentation.auth.TokenManager
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
                                        Entry(
                                            getYByDayOfWeek(dayOfWeek),
                                            it.expenditure?.toFloat() ?: 0.0f
                                        )
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
                        val categoryList = TokenManager.getCategoryOrder()

                        val categories = categories.map {
                            StatWeeklyUiState.PieChartData(
                                categoryId = it.categoryId,
                                category = SpendingCategory.fromName(it.categoryName),
                                expenditure = it.totalExpenditure,
                                color = Colors.fromRGB(
                                    red = it.red,
                                    blue = it.blue,
                                    green = it.green
                                )
                            )
                        }.sortedByDescending { it.expenditure }

                        _uiState.update { prev ->
                            prev.copy(
                                compareList = mutableListOf<StatWeeklyUiState.CompareData>().apply {
                                    // 조건에 맞는 경우에만 OthersCompare 추가
                                    if (!age.isNullOrEmpty() && !job.isNullOrEmpty() && averageExpenditure != null && myAverageExpenditure != null && topPercent != null) {
                                        add(
                                            StatWeeklyUiState.CompareData.OthersCompare(
                                                target = "${Age.getEnToKor(age!!)} ${Job.getEnToKor(job!!)}",
                                                targetSpending = averageExpenditure ?: 0,
                                                mySpending = myAverageExpenditure,
                                                spendingUnit = SpendingUnit.WEEK,
                                                percentile = topPercent ?: 0
                                            )
                                        )
                                    }

                                    // PastCompare는 항상 추가
                                    add(
                                        StatWeeklyUiState.CompareData.PastCompare(
                                            pastSpending = lastWeekExpenditure ?: 0,
                                            currentSpending = thisWeekExpenditure,
                                            spendingUnit = SpendingUnit.WEEK
                                        )
                                    )
                                },
                                pieChartList = categories
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

    private fun getYByDayOfWeek(dayOfWeek: Week): Float {
        return when (dayOfWeek) {
            Week.MON -> 0.0f
            Week.TUE -> 1.0f
            Week.WED -> 2.0f
            Week.THU -> 3.0f
            Week.FRI -> 4.0f
            Week.SAT -> 5.0f
            Week.SUN -> 6.0f
            Week.ERROR -> 0f
        }
    }
}