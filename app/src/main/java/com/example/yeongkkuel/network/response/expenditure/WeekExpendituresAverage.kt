package com.example.yeongkkuel.network.response.expenditure

import com.google.gson.annotations.SerializedName

data class WeekExpendituresAverage(
    @SerializedName("age") val age: String?,
    @SerializedName("job") val job: String?,
    @SerializedName("topPercent") val topPercent: Int?,
    @SerializedName("averageExpenditure") val averageExpenditure: Int?,
    @SerializedName("myAverageExpenditure") val myAverageExpenditure: Int,
    @SerializedName("lastWeekExpenditure") val lastWeekExpenditure: Int?,
    @SerializedName("thisWeekExpenditure") val thisWeekExpenditure: Int,
    @SerializedName("categories") val categories: List<Category>
) {
    data class Category(
        @SerializedName("categoryId") val categoryId: Int,
        @SerializedName("categoryName") val categoryName: String,
        @SerializedName("red") val red: Int,
        @SerializedName("green") val green: Int,
        @SerializedName("blue") val blue: Int,
        @SerializedName("totalExpenditure") val totalExpenditure: Int
    )
}
