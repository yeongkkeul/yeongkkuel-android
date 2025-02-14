package com.example.yeongkkuel.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.launch
import java.io.IOException

class HomeViewModel(private val repository: HomeRepository) : ViewModel() {
    private val _homeResponse = MutableLiveData<HomeResponse?>()
    val homeResponse: LiveData<HomeResponse?> get() = _homeResponse

    fun fetchHomeData() {
        viewModelScope.launch {
            try {
                val response = repository.getHomeData()
                if (response != null) {
                    Log.d("HomeViewModel", "✅ 홈 데이터 업데이트 완료! $response")
                    _homeResponse.postValue(response)
                } else {
                    Log.e("HomeViewModel", "🚨 홈 데이터 null 반환됨, 기본값 설정")
                    _homeResponse.postValue(
                        HomeResponse(
                            isSuccess = false,
                            code = "ERROR",
                            message = "홈 데이터를 불러올 수 없습니다.",
                            result = HomeResult(0, emptyList(), "", emptyList())
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "❌ 홈 데이터 불러오기 실패: ${e.message}")
                _homeResponse.postValue(null)
            }
        }
    }

    // ✅ ViewModelProvider.Factory 추가
    class Factory(private val repository: HomeRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}