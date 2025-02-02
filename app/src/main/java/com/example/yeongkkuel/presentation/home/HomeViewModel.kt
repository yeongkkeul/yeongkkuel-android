package com.example.yeongkkuel.presentation.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val repository = HomeRepository()

    private val _homeResponse = MutableLiveData<HomeResponse?>()
    val homeResponse: LiveData<HomeResponse?> = _homeResponse

    fun fetchHomeData() {
        viewModelScope.launch {
            try {
                val response = repository.getHomeData()
                _homeResponse.postValue(response)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "홈 데이터 조회 오류: ${e.message}")
                _homeResponse.postValue(null)
            }
        }
    }
}
