package com.example.yeongkkuel.network.request.expenditure

import com.google.gson.annotations.SerializedName

data class ExpenditureTargetRequest(
    @SerializedName("dayTargetExpenditure")
    val dayTargetExpenditure: Int
)
