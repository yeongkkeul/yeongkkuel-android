package com.example.yeongkkuel.presentation.home.entry.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.yeongkkuel.network.RetrofitClient
import com.example.yeongkkuel.presentation.botsheet.BotSheetViewModel
import com.example.yeongkkuel.presentation.home.CategoryResponse
import com.example.yeongkkuel.presentation.home.Expense
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class ExpenseViewModel() : ViewModel() {
    private val apiService = RetrofitClient.expenseApiService
    private val repository: ExpenseRepository = ExpenseRepository(apiService)

    private val _expenseResponse = MutableLiveData<ExpenseResponse?>()
    val expenseResponse: LiveData<ExpenseResponse?> get() = _expenseResponse
    private val _updateResponse = MutableLiveData<ExpenseUpdateResponse?>()
    val updateResponse: LiveData<ExpenseUpdateResponse?> get() = _updateResponse

    fun createExpense(
        expenseRequest: ExpenseRequest,
        imageFile: MultipartBody.Part?,
        onResult: (ExpenseResponse?) -> Unit
    ) {
        viewModelScope.launch {
            val response = repository.createExpense(expenseRequest, imageFile)
            if (response != null && response.isSuccess) {
                Log.d("ExpenseViewModel", "✅ 지출 내역 저장 성공: $response")
            } else {
                Log.e("ExpenseViewModel", "🚨 지출 내역 저장 실패 또는 응답 없음")
            }
            onResult(response)
        }
    }

    fun updateExpense(
        expenseId: Int,
        request: ExpenseUpdateRequest,
        imageFile: MultipartBody.Part?,
        onComplete: (ExpenseUpdateResponse?) -> Unit
    ) {
        viewModelScope.launch {
            val response = repository.updateExpense(expenseId, request, imageFile)
            _updateResponse.postValue(response)
            onComplete(response)
        }
    }

    fun deleteExpense(expenseId: Int, isSuccess: () -> Unit, isFail: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.deleteExpense(expenseId)
                isSuccess() // 삭제 성공
            } catch (e: Exception) {
                isFail() // 네트워크 오류 등 예외 발생 시
            }
        }
    }
}
