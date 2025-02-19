package com.example.yeongkkuel.network.service

import com.example.yeongkkuel.presentation.home.entry.data.ExpenseDeleteResponse
import com.example.yeongkkuel.presentation.home.entry.data.ExpenseResponse
import com.example.yeongkkuel.presentation.home.entry.data.ExpenseUpdateResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ExpenseApiService {

    @Multipart
    @POST("/api/expense")
    suspend fun createExpense(
        @Part("request") request: RequestBody, // JSON 변환된 request 추가
        @Part expenseImage: MultipartBody.Part? // 선택적 이미지 첨부
    ): Response<ExpenseResponse>


    @Multipart
    @PATCH("/api/expense/{expenseId}")
    suspend fun updateExpense(
        @Path("expenseId") expenseId: Int,
        @Part("day") day: RequestBody,
        @Part("categoryId") categoryId: RequestBody,
        @Part("content") content: RequestBody,
        @Part("amount") amount: RequestBody,
        @Part expenseImage: MultipartBody.Part? // 파일 첨부 (선택사항)
    ): Response<ExpenseUpdateResponse>

    @DELETE("/api/expense/{expenseId}")
    suspend fun deleteExpense(
        @Path("expenseId") expenseId: Int
    ): Response<ExpenseDeleteResponse> // 응답을 처리할 데이터 클래스 사용
}
