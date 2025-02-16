package com.example.yeongkkuel.presentation.home.entry.data

import android.util.Log
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ExpenseRepository(private val api: ExpenseApiService) {

    suspend fun createExpense(expenseRequest: ExpenseRequest, imageFile: MultipartBody.Part?): ExpenseResponse? {
        Log.d("ExpenseRepository", "🚀 지출 내역 API 요청: $expenseRequest")

        return try {
            val response = api.createExpense(
                day = expenseRequest.day,  // ✅ 그냥 String 그대로 전달
                categoryId = expenseRequest.categoryId.toString().toRequestBody(),
                content = expenseRequest.content,
                amount = expenseRequest.amount.toString().toRequestBody(),
                isExpense = expenseRequest.isExpense.toString().toRequestBody(),
                sendChatRoom = expenseRequest.sendChatRoom.toString().toRequestBody(),
                expenseImage = imageFile
            )

            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d("ExpenseRepository", "✅ 지출 내역 저장 성공: $responseBody")
                responseBody
            } else {
                Log.e("ExpenseRepository", "🚨 API 요청 실패: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("ExpenseRepository", "🚨 네트워크 오류 발생: ${e.message}")
            null
        }
    }

    suspend fun updateExpense(expenseId: Int, request: ExpenseUpdateRequest, imageFile: MultipartBody.Part?): ExpenseUpdateResponse? {
        return try {
            Log.d("ExpenseRepository", "🚀 지출 내역 수정 요청: $request")

            val response = api.updateExpense(
                expenseId = expenseId,
                day = request.day,
                categoryId = request.categoryId.toString().toRequestBody(),
                content = request.content,
                amount = request.amount.toString().toRequestBody(),
                expenseImage = imageFile
            )

            if (response.isSuccessful) {
                response.body() ?: run {
                    Log.e("ExpenseRepository", "🚨 응답이 null입니다.")
                    null
                }
            } else {
                Log.e("ExpenseRepository", "🚨 API 요청 실패: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("ExpenseRepository", "🚨 네트워크 오류 발생: ${e.message}")
            null
        }
    }

    suspend fun deleteExpense(expenseId: Int): ExpenseDeleteResponse? {
        return try {
            val response = api.deleteExpense(expenseId)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
