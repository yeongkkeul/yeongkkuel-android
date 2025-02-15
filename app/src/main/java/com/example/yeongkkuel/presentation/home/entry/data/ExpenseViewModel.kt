package com.example.yeongkkuel.presentation.home.entry.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {

    private val _expenseResponse = MutableLiveData<ExpenseResponse?>()
    val expenseResponse: LiveData<ExpenseResponse?> get() = _expenseResponse
    private val _updateResponse = MutableLiveData<ExpenseUpdateResponse?>()
    val updateResponse: LiveData<ExpenseUpdateResponse?> get() = _updateResponse

    fun createExpense(expenseRequest: ExpenseRequest, onResult: (ExpenseResponse?) -> Unit) {
        Log.d("ExpenseViewModel", "📌 createExpense() 호출됨 - 요청 데이터: $expenseRequest") // ✅ 요청 로그 추가

        viewModelScope.launch {
            val response = repository.createExpense(expenseRequest)
            if (response != null && response.isSuccess) {
                Log.d("ExpenseViewModel", "✅ 지출 내역 저장 성공: $response") // ✅ 성공 로그
            } else {
                Log.e("ExpenseViewModel", "❌ 지출 내역 저장 실패 또는 응답 없음")
            }
            onResult(response)
        }
    }

    fun updateExpense(expenseId: Int, request: ExpenseUpdateRequest, onComplete: (ExpenseUpdateResponse?) -> Unit) {
        viewModelScope.launch {
            val response = repository.updateExpense(expenseId, request)
            _updateResponse.postValue(response)
            onComplete(response)
        }
    }


    // ✅ ViewModelProvider.Factory 추가
    class Factory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ExpenseViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
