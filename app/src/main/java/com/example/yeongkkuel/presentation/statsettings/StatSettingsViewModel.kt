package com.example.yeongkkuel.presentation.statsettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class StatSettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(StatSettingsUiState.init())
    val uiState = _uiState.asStateFlow()

    fun setAverage(income: Int, outcome: Int) {
        _uiState.update { prev ->
            prev.copy(
                averageIncome = income,
                averageOutcome = outcome,
                recommendStep = RecommendStep.RATIO
            )
        }
    }

    fun setRatio(ratio: Int) {
        _uiState.update { prev ->
            //{월 평균 수입 * (1-목표 저축 비율/100) + 월 평균  지출}/2 의 백의 자리 올림
            val recommendSpending =
                ((prev.averageIncome!! * (1 - ratio / 100.0)) + prev.averageOutcome!!) / 2

            val roundedSpending = (kotlin.math.ceil(recommendSpending / 100.0) * 100).toInt()

            prev.copy(
                targetRatio = ratio,
                recommendSpending = roundedSpending,
                recommendStep = RecommendStep.SET
            )
        }
    }

    fun prevPage(isFirstPage: () -> Unit){
        _uiState.update {prev->
            when(prev.recommendStep){
                RecommendStep.AVERAGE -> {
                    isFirstPage()
                    prev.copy(recommendStep = RecommendStep.AVERAGE)
                }
                RecommendStep.RATIO ->
                    prev.copy(recommendStep = RecommendStep.AVERAGE)
                RecommendStep.SET ->
                    prev.copy(recommendStep = RecommendStep.RATIO)
            }
        }
    }


    fun setTargetSpending(targetSpending: Int, isSuccess: () -> Unit) = viewModelScope.launch {
        try {
            isSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}