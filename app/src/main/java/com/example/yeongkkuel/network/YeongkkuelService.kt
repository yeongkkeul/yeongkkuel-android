package com.example.yeongkkuel.network

import com.example.yeongkkuel.network.request.expenditure.ExpenditureTargetRequest
import com.example.yeongkkuel.network.response.Response
import com.example.yeongkkuel.network.response.expenditure.DayExpenditureResponse
import com.example.yeongkkuel.network.response.expenditure.MonthExpendituresCalendar
import com.example.yeongkkuel.network.response.expenditure.MonthExpendituresCategory
import com.example.yeongkkuel.network.response.expenditure.MonthlyAverageExpenditureResponse
import com.example.yeongkkuel.network.response.expenditure.WeekExpenditureExpensesResponse
import com.example.yeongkkuel.network.response.expenditure.WeekExpendituresAverage
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface YeongkkuelService {

    //stat
    @GET("/api/expenditures/day")
    suspend fun getExpendituresDay(
    ): retrofit2.Response<Response<DayExpenditureResponse>>

    @GET("/api/expenditures/week/expenses")
    suspend fun getExpendituresWeekExpenses(
    ): Response<WeekExpenditureExpensesResponse>

    @GET("/api/expenditures/week/average")
    suspend fun getExpendituresWeekAverage(
    ): Response<WeekExpendituresAverage>

    @GET("/api/expenditures/month/{year}/{month}")
    suspend fun getExpendituresMonthCalendar(
        @Path("year") year: Int,
        @Path("month") month: Int
    ): Response<MonthExpendituresCalendar>

    @GET("/api/expenditures/{year}/{month}/{day}")
    suspend fun getExpendituresMonthCategory(
        @Path("year") year: Int,
        @Path("month") month: Int,
        @Path("day") day: Int,
    ): Response<MonthExpendituresCategory>

    @POST("/api/expenditures/target")
    suspend fun postExpendituresTarget(
        @Body request: ExpenditureTargetRequest
    )

    @GET("/api/expenditures/target/recommendation")
    suspend fun getExpenditureAverageMonthly(
    ): Response<MonthlyAverageExpenditureResponse>
}