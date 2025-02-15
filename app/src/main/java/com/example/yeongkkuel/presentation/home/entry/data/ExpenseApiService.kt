package com.example.yeongkkuel.presentation.home.entry.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ExpenseApiService {

    @POST("/api/expense")
    suspend fun createExpense(
        @Body expenseRequest: ExpenseRequest
    ): Response<ExpenseResponse>

    @PUT("/api/expense/{expenseId}")
    suspend fun updateExpense(
        @Path("expenseId") expenseId: Int,
        @Body request: ExpenseUpdateRequest
    ): Response<ExpenseUpdateResponse>
}
