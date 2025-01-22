package com.example.yeongkkuel.presentation.statsettings

data class StatSettingsUiState(
    val targetSpending: Int? = null,
    val averageIncome: Int? = null,
    val averageOutcome: Int? = null,
    val targetRatio: Int? = null,
    val recommendSpending: Int? = null,
    val recommendStep: RecommendStep = RecommendStep.AVERAGE
) {
    companion object {
        fun init() = StatSettingsUiState()
    }
}

enum class RecommendStep{
    AVERAGE, RATIO, SET
}