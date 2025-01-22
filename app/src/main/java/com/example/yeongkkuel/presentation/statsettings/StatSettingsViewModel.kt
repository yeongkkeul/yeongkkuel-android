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
            prev.copy(
                targetRatio = ratio,
                recommendStep = RecommendStep.SET
            )
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