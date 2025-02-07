package com.example.yeongkkuel.network

import com.example.yeongkkuel.network.request.expenditure.ExpenditureTargetRequest
import com.example.yeongkkuel.network.response.Response
import com.example.yeongkkuel.network.response.expenditure.DayExpenditureResponse
import com.example.yeongkkuel.network.response.expenditure.MonthExpendituresCalendar
import com.example.yeongkkuel.network.response.expenditure.MonthExpendituresCategory
import com.example.yeongkkuel.network.response.expenditure.MonthlyAverageExpenditureResponse
import com.example.yeongkkuel.network.response.expenditure.WeekExpenditureExpensesResponse
import com.example.yeongkkuel.network.response.expenditure.WeekExpendituresAverage
import com.example.yeongkkuel.presentation.home.category.data.CategoryRequest
import com.example.yeongkkuel.presentation.home.category.data.CategoryResponse

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
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


    // Category
    @GET("/api/category/categories")
    suspend fun getCategories(): Response<List<CategoryResponse>>

    @GET("/api/category/{category_id}")
    suspend fun getCategoryDetail(
        @Path("category_id") categoryId: Int
    ): Response<CategoryResponse>

    @POST("/api/category")
    suspend fun addCategory(
        @Body request: CategoryRequest
    ): Response<Unit>

    @PATCH("/api/category/{category_id}")
    suspend fun updateCategory(
        @Path("category_id") categoryId: Int,
        @Body request: CategoryRequest
    ): Response<Unit>

    @DELETE("/api/category/{category_id}")
    suspend fun deleteCategory(
        @Path("category_id") categoryId: Int
    ): Response<Unit>

}