package com.example.yeongkkuel.presentation.network

import com.example.yeongkkuel.presentation.network.response.Response
import com.example.yeongkkuel.presentation.network.response.expenditure.DayExpenditureResponse
import retrofit2.http.GET

interface YeongkkuelService {
    @GET("/api/expenditures/day")
    fun getExpendituresDay(): Response<DayExpenditureResponse>
}