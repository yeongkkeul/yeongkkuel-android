package com.example.yeongkkuel.network.response.expenditure

import com.google.gson.annotations.SerializedName

data class MonthlyAverageExpenditureResponse(
    @SerializedName("averageExpenditure") val averageExpenditure: Int
)