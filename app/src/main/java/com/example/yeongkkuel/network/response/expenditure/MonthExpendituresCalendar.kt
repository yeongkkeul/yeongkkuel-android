package com.example.yeongkkuel.network.response.expenditure

import com.google.gson.annotations.SerializedName

data class MonthExpendituresCalendar(
    @SerializedName("dayTargetExpenditure") val dayTargetExpenditure: Int?,
    @SerializedName("totalMonthExpenditure") val totalMonthExpenditure: Int,
    @SerializedName("selectedMonthExpenses") val selectedMonthExpenses: List<Expense>,
    @SerializedName("previousMonthExpenses") val previousMonthExpenses: List<Expense>,
    @SerializedName("achieveDays") val achieveDays: Int?,
    @SerializedName("rewards") val rewards: Int
) {
    data class Expense(
        @SerializedName("year") val year: Int,
        @SerializedName("month") val month: Int,
        @SerializedName("expenseDate") val expenseDate: String,
        @SerializedName("expenditure") val expenditure: Int
    )
}
