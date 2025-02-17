package com.example.yeongkkuel.presentation.home.entry.data

import android.util.Log
import com.example.yeongkkuel.network.service.ExpenseApiService
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class ExpenseRepository(private val api: ExpenseApiService) {

    suspend fun createExpense(expenseRequest: ExpenseRequest, imageFile: MultipartBody.Part?): ExpenseResponse? {
        Log.d("ExpenseRepository", "🚀 지출 내역 API 요청: $expenseRequest")

        return try {
            // 1. expenseRequest의 텍스트 필드를 하나의 Map으로 묶어 JSON 문자열 생성
            val expenseMap = mapOf(
                "day" to expenseRequest.day,
                "categoryId" to expenseRequest.categoryId,
                "content" to expenseRequest.content,
                "amount" to expenseRequest.amount,
                "isExpense" to expenseRequest.isExpense,
                "sendChatRoom" to expenseRequest.sendChatRoom
            )
            val json = Gson().toJson(expenseMap)
            val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            // 2. API 호출 시 "request" 파트에 JSON, 이미지 파일은 별도로 전달
            val response = api.createExpense(
                requestBody = requestBody,
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

            // 1. 수정할 필드를 하나의 JSON 객체로 생성
            val updateMap = mapOf(
                "day" to request.day,
                "categoryId" to request.categoryId,
                "content" to request.content,
                "amount" to request.amount
            )
            val json = Gson().toJson(updateMap)
            val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            // 2. API 호출 (updateExpense 인터페이스가 JSON request를 받도록 수정되어 있어야 함)
            val response = api.updateExpense(
                expenseId = expenseId,
                requestBody = requestBody,
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
