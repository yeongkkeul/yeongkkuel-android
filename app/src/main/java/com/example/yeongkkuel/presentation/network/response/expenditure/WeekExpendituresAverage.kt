package com.example.yeongkkuel.presentation.network.response.expenditure

import com.google.gson.annotations.SerializedName

data class WeekExpendituresAverage(
    @SerializedName("age") val age: Int,
    @SerializedName("job") val job: String,
    @SerializedName("topPercent") val topPercent: Int,
    @SerializedName("averageExpenditure") val averageExpenditure: Int,
    @SerializedName("myAverageExpenditure") val myAverageExpenditure: Int,
    @SerializedName("lastWeekExpenditure") val lastWeekExpenditure: Int,
    @SerializedName("thisWeekExpenditure") val thisWeekExpenditure: Int,
    @SerializedName("categories") val categories: List<Category>
) {
    data class Category(
        @SerializedName("categoryName") val categoryName: String,
        @SerializedName("categoryColor") val categoryColor: String,
        @SerializedName("totalExpenditure") val totalExpenditure: Int
    )
}
