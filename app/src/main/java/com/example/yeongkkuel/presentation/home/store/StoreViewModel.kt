package com.example.yeongkkuel.presentation.home.store

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yeongkkuel.R
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

    private val _productUiState = MutableLiveData<ProductUiState>()
    val productUiState: LiveData<ProductUiState> = _productUiState

    private val _showFailureDialog = MutableLiveData<Boolean>()
    val showFailureDialog: LiveData<Boolean> = _showFailureDialog

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    var currentReward: Int = 0 // ✅ 보유 리워드 저장 변수 추가

    init {
        _productUiState.value = ProductUiState()
    }

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

                if (response != null) {
                    Log.d("StoreViewModel", "스킨 구매 응답 코드: ${response.code}, 메시지: ${response.message}")

                    if (response.code == "REWARD4001") {
                        _showFailureDialog.postValue(true) // ✅ 리워드 부족 시 다이얼로그 활성화
                    } else if (response.isSuccess) {
                        currentReward -= reward // ✅ 리워드 차감
                    }

                    _purchaseResponse.postValue(response)
                } else {
                    _purchaseResponse.postValue(null)
                }
            } catch (e: Exception) {
                Log.e("StoreViewModel", "스킨 구매 오류: ${e.message}")
                _purchaseResponse.postValue(null)
            }
        }
    }

    fun resetFailureDialog() {
        _showFailureDialog.postValue(false)
    }

    fun fetchShopData(itemType: String) {
        viewModelScope.launch {
            try {
                Log.d("StoreViewModel", "🛒 Fetching shop data for itemType: $itemType")

                val response = repository.getShopData(itemType)

                if (response?.isSuccess == true) {
                    Log.d("StoreViewModel", "✅ 상점 데이터 조회 성공: ${response.result.itemList}")

                    _shopResponse.postValue(response)

                    // ✅ 보유 리워드 업데이트
                    response.result.myReward?.let {
                        currentReward = it
                    }

                    val shopItems = response.result.itemList.map { shopItem ->
                        ProductUiState.Product(
                            id = shopItem.id,
                            name = shopItem.itemName,
                            price = shopItem.price,
                            category = ProductCategory.valueOf(response.result.itemType),
                            imageUrl = shopItem.itemImg
                        )
                    }

                    _productUiState.postValue(
                        ProductUiState(
                            totalProducts = shopItems.size,
                            productList = shopItems
                        )
                    )
                } else {
                    Log.e("StoreViewModel", "❌ 상점 데이터 조회 실패: ${response?.message ?: "오류 발생"}")
                    _shopResponse.postValue(null)
                }
            } catch (e: Exception) {
            Log.e("StoreViewModel", "❌ 상점 데이터 조회 오류: ${e.message}")
                _shopResponse.postValue(null)
            }
        }
    }
}

