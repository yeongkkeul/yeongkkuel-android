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
                        fetchShopData("MY")
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

    fun fetchShopData(itemType: String) {
        viewModelScope.launch {
            try {
                Log.d("HomeViewModel", "🛍️ ${itemType} 데이터 요청 중...")

                val response = repository.getShopData(itemType)

                if (response?.isSuccess == true) {
                    Log.d("HomeViewModel", "✅ ${itemType} 데이터 수신 완료: ${response.result.itemList}")

                    if (itemType == "MY") {
                        Log.d("HomeViewModel", "🟢 MY 탭 데이터 갱신 중...")
                        val mySkinList = response.result.mySkin.map { mySkin ->
                            MySkin(
                                itemName = mySkin.itemName,
                                itemType = mySkin.itemType,
                                imgUrl = mySkin.imgUrl ?: ""
                            )
                        }

                        val shopItemList = response.result.itemList.map { shopItem ->
                            MySkin(  // ✅ ShopItem을 MySkin으로 변환
                                itemName = shopItem.itemName,
                                itemType = shopItem.itemType,
                                imgUrl = shopItem.itemImg ?: ""
                            )
                        }

                        // ✅ mySkin + shopItem을 합쳐서 관리
                        _homeResult.postValue(
                            HomeResult(
                                myReward = response.result.myReward,
                                mySkin = mySkinList + shopItemList, // 두 리스트를 합침
                                today = _homeResult.value?.today ?: "",
                                categories = _homeResult.value?.categories ?: emptyList()
                            )
                        )
                    }
                } else {
                    Log.e("HomeViewModel", "❌ ${itemType} 데이터 가져오기 실패: ${response?.message ?: "오류 발생"}")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "❌ ${itemType} 데이터 요청 오류: ${e.message}")
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
