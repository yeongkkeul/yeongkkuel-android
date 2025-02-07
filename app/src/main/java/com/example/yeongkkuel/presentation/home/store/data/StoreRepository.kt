package com.example.yeongkkuel.presentation.home.store.data

import android.util.Log
import com.example.yeongkkuel.network.RetrofitClient
import com.google.gson.Gson

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

            if (response.isSuccessful) {
                Log.d("StoreRepository", "스킨 구매 성공: ${response.body()}")
                return response.body()
            } else {
                val errorResponse = response.errorBody()?.string()
                Log.e("StoreRepository", "스킨 구매 실패: 응답 코드 ${response.code()} - $errorResponse")

                // 에러 응답을 JSON 객체로 변환
                val gson = Gson()
                val errorObj = gson.fromJson(errorResponse, SkinPurchaseResponse::class.java)
                errorObj
            }
        } catch (e: Exception) {
            Log.e("StoreRepository", "스킨 구매 API 오류: ${e.message}")
            null
        }
    }

    suspend fun getShopData(itemType: String): ShopResponse? {
        return try {
            Log.d("StoreRepository", "🛒 API 요청: itemType = $itemType") // ✅ API 요청 로그 추가
            val response = api.getShopData(itemType)

            if (response.isSuccessful) {
                Log.d("StoreRepository", "✅ API 응답 성공: ${response.body()}") // ✅ 응답 성공 로그
                response.body()
            } else {
                Log.e("StoreRepository", "❌ API 응답 실패: ${response.errorBody()?.string()}") // ✅ 응답 실패 로그
                null
            }
        } catch (e: Exception) {
            Log.e("StoreRepository", "❌ API 요청 중 오류 발생: ${e.message}") // ✅ 예외 발생 로그
            null
        }
    }

}
