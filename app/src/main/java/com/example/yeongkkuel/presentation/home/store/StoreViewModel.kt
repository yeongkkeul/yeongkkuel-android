package com.example.yeongkkuel.presentation.home.store

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yeongkkuel.presentation.home.store.data.ShopResponse
import com.example.yeongkkuel.presentation.home.store.data.SkinEquipResponse
import com.example.yeongkkuel.presentation.home.store.data.SkinPurchaseResponse
import com.example.yeongkkuel.presentation.home.store.data.StoreRepository
import kotlinx.coroutines.launch


class StoreViewModel : ViewModel() {

    private val repository = StoreRepository()

    private val _equipResponse = MutableLiveData<SkinEquipResponse?>()
    val equipResponse: LiveData<SkinEquipResponse?> = _equipResponse

    private val _purchaseResponse = MutableLiveData<SkinPurchaseResponse?>()
    val purchaseResponse: LiveData<SkinPurchaseResponse?> = _purchaseResponse

    private val _shopResponse = MutableLiveData<ShopResponse?>()
    val shopResponse: LiveData<ShopResponse?> = _shopResponse

    fun saveEquippedSkins(purchaseIds: List<Int>) {
        viewModelScope.launch {
            try {
                val response = repository.saveEquippedSkins(purchaseIds)
                _equipResponse.postValue(response)
            } catch (e: Exception) {
                Log.e("StoreViewModel", "스킨 착용 저장 오류: ${e.message}")
                _equipResponse.postValue(null)
            }
        }
    }

    fun purchaseSkin(itemId: Int, itemType: String, itemName: String, reward: Int) {
        viewModelScope.launch {
            try {
                val response = repository.purchaseSkin(itemId, itemType, itemName, reward)
                _purchaseResponse.postValue(response)
            } catch (e: Exception) {
                Log.e("StoreViewModel", "스킨 구매 오류: ${e.message}")
                _purchaseResponse.postValue(null)
            }
        }
    }
    fun fetchShopData(itemType: String) {
        viewModelScope.launch {
            try {
                val response = repository.getShopData(itemType)
                _shopResponse.postValue(response)
            } catch (e: Exception) {
                Log.e("StoreViewModel", "상점 데이터 조회 오류: ${e.message}")
                _shopResponse.postValue(null)
            }
        }
    }
}
