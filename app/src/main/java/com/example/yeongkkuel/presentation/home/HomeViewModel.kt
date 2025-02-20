package com.example.yeongkkuel.presentation.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: HomeRepository) : ViewModel() {
    private val _homeResult = MutableLiveData<HomeResult?>() // HomeResult로 변경
    val homeResult: LiveData<HomeResult?> get() = _homeResult

    private val _yesterdayReward = MutableLiveData<Int?>() //  HomeResult로 변경
    val yesterdayReward: LiveData<Int?> get() = _yesterdayReward

    fun fetchHomeData() {
        viewModelScope.launch {
            try {
                Log.d("HomeViewModel", "홈 데이터 요청 시작...")
                val response = repository.getHomeData()

                if (response != null) {
                    Log.d("HomeViewModel", "홈 데이터 수신 완료: $response")

                    if (response.mySkin.isNullOrEmpty()) {
                        Log.e("HomeViewModel", "`mySkin`이 비어 있음. 서버 응답 확인 필요!")
                    } else {
                        Log.d("HomeViewModel", "`mySkin` 데이터 업데이트 완료: ${response.mySkin}")
                    }

                    _homeResult.postValue(response.copy(mySkin = response.mySkin ?: emptyList()))
                } else {
                    Log.e("HomeViewModel", "홈 데이터 null 반환됨. API 응답 확인 필요!")
                    _homeResult.postValue(HomeResult(myReward = 0, mySkin = emptyList(), today = "", categories = emptyList()))
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", " 홈 데이터 불러오기 실패: ${e.message}")
                _homeResult.postValue(null)
            }
        }
    }

    fun fetchYesterdayReward() {
        viewModelScope.launch {
            try {
                val response = repository.getYesterdayReward()
                if (response != null && response.isSuccess) {
                    Log.d("HomeViewModel", " 어제 리워드 데이터 업데이트 완료! ${response.result.yesterdayReward}")
                    _yesterdayReward.postValue(response.result.yesterdayReward)
                } else {
                    Log.e("HomeViewModel", "어제 리워드 데이터 null 반환됨, 기본값 0 설정")
                    _yesterdayReward.postValue(0)
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "어제 리워드 데이터 불러오기 실패: ${e.message}")
                _yesterdayReward.postValue(0)
            }
        }
    }
    fun addMySkin(newSkin: MySkin) {
        val updatedList = _homeResult.value?.mySkin?.toMutableList() ?: mutableListOf()
        updatedList.add(newSkin)

        _homeResult.value = _homeResult.value?.copy(mySkin = updatedList)
        Log.d("HomeViewModel", "mySkin 리스트 업데이트 완료: ${updatedList.size}개")
    }

    fun saveEquippedSkins(purchaseIds: List<Int>) {
        viewModelScope.launch {
            try {
                Log.d("HomeViewModel", "스킨 착용 저장 요청 - purchaseIds: $purchaseIds")

                val response = repository.saveEquippedSkins(purchaseIds)

                if (response?.isSuccess == true) {
                    Log.d("HomeViewModel", "스킨 착용 저장 성공! 홈 데이터 다시 불러오기")

                    fetchHomeData()
                    // `mySkin` 데이터를 포함한 최신 홈 데이터 다시 불러오기
                } else {
                    Log.e("HomeViewModel", "스킨 착용 저장 실패: ${response?.message}")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "스킨 착용 저장 API 오류: ${e.message}")
            }
        }
    }

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