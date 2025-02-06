package com.example.yeongkkuel.network.response.expenditure

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
        @SerializedName("expenseName") val expenseName: String,       // 🔹 지출 이름
        @SerializedName("expenseAmount") val expenseAmount: Int,      // 🔹 금액
        @SerializedName("expenseDate") val expenseDate: String,       // 🔹 지출 날짜 추가
        @SerializedName("expenseContent") val expenseContent: String, // 🔹 지출 내용 추가
        @SerializedName("expensePhotoUrl") val expensePhotoUrl: String // 🔹 사진 URL 추가
    )
}