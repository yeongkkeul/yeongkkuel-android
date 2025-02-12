package com.example.yeongkkuel.network.response.expenditure

import com.google.gson.annotations.SerializedName

data class WeekExpenditureExpensesResponse(
    @SerializedName("weekExpenditure") val weekExpenditure: Int,
    @SerializedName("dayTargetExpenditure") val dayTargetExpenditure: Int?,
    @SerializedName("expenses") val expenses: List<Expense>
) {
    data class Expense(
        @SerializedName("expenseDate") val expenseDate: String, // 2025-01-11, Saturday 형식
        @SerializedName("expediture") val expenditure: Int?
    )
}