package com.example.yeongkkuel.network.response.expenditure

import com.google.gson.annotations.SerializedName

data class DayExpenditureResponse(
    @SerializedName("dayTargetExpenditure") val dayTargetExpenditure: Int
)