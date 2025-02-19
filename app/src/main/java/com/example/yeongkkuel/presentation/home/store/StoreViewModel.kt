package com.example.yeongkkuel.presentation.home.store

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yeongkkuel.presentation.home.HomeViewModel
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

    var currentReward: Int = 0 // 보유 리워드 저장 변수 추가

    init {
        _productUiState.value = ProductUiState()
    }

    fun saveEquippedSkins(purchaseIds: List<Int>, homeViewModel: HomeViewModel) {
        viewModelScope.launch {
            try {
                Log.d("StoreViewModel", "saveEquippedSkins() 호출 - purchaseIds: $purchaseIds")

                val response = repository.saveEquippedSkins(purchaseIds)
                _equipResponse.postValue(response)

                if (response?.isSuccess == true) {
                    Log.d("StoreViewModel", "스킨 착용 저장 성공 - HomeViewModel 업데이트 호출")
                    homeViewModel.fetchHomeData()
                } else {
                    Log.e("StoreViewModel", "스킨 착용 저장 실패")
                }
            } catch (e: Exception) {
                Log.e("StoreViewModel", "스킨 착용 저장 오류: ${e.message}")
            }
        }
    }

    fun purchaseSkin(itemId: Int, itemType: String, itemName: String, reward: Int, homeViewModel: HomeViewModel) {
        viewModelScope.launch {
            try {
                Log.d("StoreViewModel", "purchaseSkin() 호출 - itemId: $itemId, itemType: $itemType, itemName: $itemName, reward: $reward")

                val response = repository.purchaseSkin(itemId, itemType, itemName, reward)

                if (response != null && response.isSuccess) {
                    Log.d("StoreViewModel", "스킨 구매 성공 - 응답 코드: ${response.code}, 메시지: ${response.message}, 결과: ${response.result}")

                    fetchShopData("MY")

                    _purchaseResponse.postValue(response)

                    homeViewModel.fetchHomeData()
                } else {
                    Log.e("StoreViewModel", " 스킨 구매 실패 - ${response?.message ?: "응답 없음"}")
                    _purchaseResponse.postValue(null)
                }
            } catch (e: Exception) {
                Log.e("StoreViewModel", " 스킨 구매 오류: ${e.message}")
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
                Log.d("StoreViewModel", " ${itemType} 데이터 요청 중...")

                val response = repository.getShopData(itemType)

                if (response?.isSuccess == true) {
                    Log.d("StoreViewModel", " ${itemType} 데이터 수신 완료: ${response.result.itemList}")

                    _shopResponse.postValue(response)

                    if (itemType == "MY") {
                        Log.d("StoreViewModel", " MY 탭 데이터 갱신 중...")
                        _productUiState.postValue(
                            ProductUiState(
                                totalProducts = response.result.itemList.size,
                                productList = response.result.itemList.map { shopItem ->
                                    ProductUiState.Product(
                                        id = shopItem.id,
                                        name = shopItem.itemName,
                                        price = shopItem.price ?: 0,
                                        category = ProductCategory.MY,
                                        imageUrl = shopItem.itemImg,
                                        itemType = "MY"
                                    )
                                }
                            )
                        )
                    }
                } else {
                    Log.e("StoreViewModel", " ${itemType} 데이터 가져오기 실패: ${response?.message ?: "오류 발생"}")
                }
            } catch (e: Exception) {
                Log.e("StoreViewModel", " ${itemType} 데이터 요청 오류: ${e.message}")
            }
        }
    }


}

