package com.example.yeongkkuel.presentation.home.entry.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.yeongkkuel.presentation.botsheet.BotSheetViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class ExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {

    private val _expenseResponse = MutableLiveData<ExpenseResponse?>()
    val expenseResponse: LiveData<ExpenseResponse?> get() = _expenseResponse
    private val _updateResponse = MutableLiveData<ExpenseUpdateResponse?>()
    val updateResponse: LiveData<ExpenseUpdateResponse?> get() = _updateResponse
    private val _deleteResult = MutableLiveData<Boolean>()
    val deleteResult: LiveData<Boolean> get() = _deleteResult


    private val botSheetViewModel: BotSheetViewModel by lazy {
        BotSheetViewModel()
    }

    fun createExpense(expenseRequest: ExpenseRequest, imageFile: MultipartBody.Part?, onResult: (ExpenseResponse?) -> Unit) {
        viewModelScope.launch {
            val response = repository.createExpense(expenseRequest, imageFile)
            if (response != null && response.isSuccess) {
                Log.d("ExpenseViewModel", "✅ 지출 내역 저장 성공: $response")

                botSheetViewModel.getSpendingList()
            } else {
                Log.e("ExpenseViewModel", "🚨 지출 내역 저장 실패 또는 응답 없음")
            }
            onResult(response)
        }
    }

    fun updateExpense(expenseId: Int, request: ExpenseUpdateRequest, imageFile: MultipartBody.Part?, onComplete: (ExpenseUpdateResponse?) -> Unit) {
        viewModelScope.launch {
            val response = repository.updateExpense(expenseId, request, imageFile)
            _updateResponse.postValue(response)
            onComplete(response)
        }
    }

    fun deleteExpense(expenseId: Int) {
        viewModelScope.launch {
            try {
                val response = repository.deleteExpense(expenseId)
                if (response?.isSuccess == true) {
                    _deleteResult.postValue(true) // 삭제 성공
                } else {
                    _deleteResult.postValue(false) // 삭제 실패
                }
            } catch (e: Exception) {
                _deleteResult.postValue(false) // 네트워크 오류 등 예외 발생 시
            }
        }
    }

    // ViewModelProvider.Factory 추가
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
