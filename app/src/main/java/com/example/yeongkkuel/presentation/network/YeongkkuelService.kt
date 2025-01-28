package com.example.yeongkkuel.presentation.network

import com.example.yeongkkuel.presentation.network.request.expenditure.ExpenditureTargetRequest
import com.example.yeongkkuel.presentation.network.response.Response
import com.example.yeongkkuel.presentation.network.response.expenditure.DayExpenditureResponse
import com.example.yeongkkuel.presentation.network.response.expenditure.MonthExpendituresCalendar
import com.example.yeongkkuel.presentation.network.response.expenditure.MonthExpendituresCategory
import com.example.yeongkkuel.presentation.network.response.expenditure.MonthlyAverageExpenditureResponse
import com.example.yeongkkuel.presentation.network.response.expenditure.WeekExpenditureExpensesResponse
import com.example.yeongkkuel.presentation.network.response.expenditure.WeekExpendituresAverage
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.Calendar
import java.util.Date

interface YeongkkuelService {

    //stat
    @GET("/api/expenditures/day")
    fun getExpendituresDay(
    ): Response<DayExpenditureResponse>

    @GET("/api/expenditures/week/expenses")
    fun getExpendituresWeekExpenses(
    ): Response<WeekExpenditureExpensesResponse>

    @GET("/api/expenditures/week/average")
    fun getExpendituresWeekAverage(
    ): Response<WeekExpendituresAverage>

    @GET("/api/expenditures/month/{year}/{month}")
    fun getExpendituresMonthCalendar(
        @Path("year") year: Int,
        @Path("month") month: Int
    ): Response<MonthExpendituresCalendar>

    @GET("/api/expenditures/{year}/{month}/{day}")
    fun getExpendituresMonthCategory(
        @Path("year") year: Int,
        @Path("month") month: Int,
        @Path("day") day: Int,
    ): Response<MonthExpendituresCategory>

    @POST("/api/expenditures/target")
    fun postExpendituresTarget(
        @Body request: ExpenditureTargetRequest
    )

    @GET("/api/expenditures/target/recommendation")
    fun getExpenditureAverageMonthly(
    ): Response<MonthlyAverageExpenditureResponse>
}