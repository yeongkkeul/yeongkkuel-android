package com.example.yeongkkuel.presentation.statsettings

data class StatSettingsUiState(
    val targetSpending: Int = 0,
    val averageIncome: Int = 0,
    val averageOutcome: Int? = null,
    val targetRatio: Int = 0,
    val recommendSpending: Int = 0,
    val recommendStep: RecommendStep = RecommendStep.AVERAGE
) {
    companion object {
        fun init() = StatSettingsUiState()
    }
}

enum class RecommendStep{
    AVERAGE, RATIO, SET
}