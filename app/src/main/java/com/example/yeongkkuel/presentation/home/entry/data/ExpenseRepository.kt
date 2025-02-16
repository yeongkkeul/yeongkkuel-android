package com.example.yeongkkuel.presentation.home.entry.data

import android.util.Log

class ExpenseRepository(private val api: ExpenseApiService) {

    suspend fun createExpense(expenseRequest: ExpenseRequest): ExpenseResponse? {
        Log.d("ExpenseRepository", "🚀 지출 내역 API 요청: $expenseRequest") // ✅ 요청 데이터 로그 추가

        return try {
            val response = api.createExpense(expenseRequest)
            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d("ExpenseRepository", "✅ 지출 내역 저장 성공: $responseBody") // ✅ 응답 데이터 로그 추가
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

    suspend fun updateExpense(expenseId: Int, request: ExpenseUpdateRequest): ExpenseUpdateResponse? {
        return try {
            Log.d("ExpenseRepository", "🚀 지출 내역 수정 요청: $request")
            val response = api.updateExpense(expenseId, request)
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
}
