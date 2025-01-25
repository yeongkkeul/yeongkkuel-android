package com.example.yeongkkuel.presentation.network.response.expenditure

import com.google.gson.annotations.SerializedName

data class MonthExpendituresCalendar(
    @SerializedName("selectedMonthExpenses") val selectedMonthExpenses: List<Expense>,
    @SerializedName("previousMonthExpenses") val previousMonthExpenses: List<Expense>,
    @SerializedName("achievedDays") val achievedDays: Int,
    @SerializedName("rewards") val rewards: Int
) {
    data class Expense(
        @SerializedName("year") val year: Int,
        @SerializedName("month") val month: Int,
        @SerializedName("monthExpenditure") val monthExpenditure: Int,
        @SerializedName("expenseDate") val expenseDate: String,
        @SerializedName("expenditure") val expenditure: Int
    )
}