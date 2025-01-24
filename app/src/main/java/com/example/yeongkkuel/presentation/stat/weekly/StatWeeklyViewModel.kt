package com.example.yeongkkuel.presentation.stat.weekly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yeongkkuel.presentation.network.RetrofitClient
import com.example.yeongkkuel.presentation.util.Week
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StatWeeklyViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<StatWeeklyUiState>(StatWeeklyUiState.init())
    val uiState = _uiState.asStateFlow()

    private val yeongkkuelService = RetrofitClient.yeongkkuelService

    fun getWeekExpenditureList() = viewModelScope.launch {
        try {
            yeongkkuelService.getExpendituresWeekExpenses().run {
                if (isSuccess == true) {
                    result.run {
                        _uiState.update { prev ->
                            prev.copy(
                                weekList = expenses.map {
                                    StatWeeklyUiState.DayData(getDayOfWeek(date = it.expenseDate), Entry(0f, it.expenditure.toFloat()))
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




    private fun getDayOfWeek(date: String): Week {
        val parts = date.split(", ")
        val dayOfWeek = parts[1] // "Saturday" 추출
        return Week.fromString(dayOfWeek)
    }
}