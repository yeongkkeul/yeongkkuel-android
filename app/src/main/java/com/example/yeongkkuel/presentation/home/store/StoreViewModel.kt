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

                    // REWARD4001 응답 코드일 경우, 다이얼로그 띄우기 위해 true로 설정
                    if (response.code == "REWARD4001") {
                        _showFailureDialog.postValue(true)
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
                Log.d("StoreViewModel", "🛒 Fetching shop data for itemType: $itemType") // ✅ 요청 로그 추가

                val response = repository.getShopData(itemType)

                if (response != null) {
                    Log.d("StoreViewModel", "✅ API 응답: $response") // ✅ 전체 응답 로그 추가
                } else {
                    Log.e("StoreViewModel", "❌ API 응답이 null입니다!")
                }

                if (response?.isSuccess == true) {
                    Log.d("StoreViewModel", "✅ 상점 데이터 조회 성공: ${response.result.itemList}") // ✅ 성공한 데이터 로그 추가

                    _shopResponse.postValue(response)

                    val shopItems = response.result.itemList.map { shopItem ->
                        ProductUiState.Product(
                            id = shopItem.id,
                            name = shopItem.itemName,
                            price = shopItem.price,
                            category = ProductCategory.valueOf(response.result.itemType),
                            imageUrl = shopItem.itemImg // ✅ 서버에서 제공하는 이미지 URL 그대로 사용
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


    private fun getDrawableFromUrl(url: String): Int {
        return when (url) {
            "swing_1.png" -> R.drawable.img_home_swing1
            "toy_1.png" -> R.drawable.img_home_toy1
            "bowl_1.png" -> R.drawable.img_home_bowl1
            "nest_1.png" -> R.drawable.img_home_nest1
            else -> R.drawable.img_home_swing1
        }
    }
}
