package com.example.yeongkkuel.network.response.expenditure

import com.google.gson.annotations.SerializedName

data class MonthExpendituresCategory(
    @SerializedName("categories")
    val categories: List<Category>
) {
    data class Category(
        @SerializedName("categoryId") val categoryId: Int,
        @SerializedName("categoryName") val categoryName: String,
        @SerializedName("red") val red: Int,               // RGB 값
        @SerializedName("green") val green: Int,           // RGB 값
        @SerializedName("blue") val blue: Int,             // RGB 값
        @SerializedName("expenses") val expenses: List<Expense>
    )

    data class Expense(
        @SerializedName("expenseId") val expenseId: Int,       // 지출 ID
        @SerializedName("expenseName") val expenseName: String, // 지출 이름
        @SerializedName("expenseAmount") val expenseAmount: Int, // 금액
        @SerializedName("imgExist") val imgExist: String
    )
}
