package com.example.yeongkkuel.presentation.network.response.expenditure

import com.google.gson.annotations.SerializedName

data class MonthExpendituresCategory(
    @SerializedName("categories")
    val categories: List<Category>
) {
    data class Category(
        @SerializedName("categoryName") val categoryName: String,
        @SerializedName("categoryColor") val categoryColor: String,
        @SerializedName("expenses") val expenses: List<Expense>
    )

    data class Expense(
        @SerializedName("expenseName") val expenseName: String,
        @SerializedName("expenseAmount") val expenseAmount: Int
    )
}