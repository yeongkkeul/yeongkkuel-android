package com.example.yeongkkuel.presentation.home.store.data

import android.util.Log
import com.example.yeongkkuel.presentation.network.RetrofitClient

class StoreRepository {
    private val api = RetrofitClient.storeapiService

    suspend fun saveEquippedSkins(purchaseIds: List<Int>): SkinEquipResponse? {
        val request = SkinEquipRequest(userItem = purchaseIds.map { SkinPurchase(it) })

        return try {
            val response = api.saveEquippedSkins(request) // ✅ Response<SkinEquipResponse> 반환

            if (response.isSuccessful) {
                response.body() // ✅ API 응답이 null일 가능성이 있음
            } else {
                Log.e("StoreRepository", "API 요청 실패: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("StoreRepository", "API 요청 중 오류 발생: ${e.message}")
            null
        }
    }

    suspend fun purchaseSkin(itemId: Int, itemType: String, itemName: String, reward: Int): SkinPurchaseResponse? {
        val request = SkinPurchaseRequest(itemId, itemType, itemName, reward)
        return try {
            val response = api.purchaseSkin(request)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            Log.e("StoreRepository", "스킨 구매 API 오류: ${e.message}")
            null
        }
    }

    suspend fun getShopData(itemType: String): ShopResponse? {
        return try {
            val response = api.getShopData(itemType)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            Log.e("StoreRepository", "상점 데이터 조회 오류: ${e.message}")
            null
        }
    }
}
