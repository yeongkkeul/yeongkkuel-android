package com.example.yeongkkuel.presentation.home

import android.util.Log
import com.example.yeongkkuel.network.RetrofitClient
import com.example.yeongkkuel.presentation.home.store.data.ShopResponse
import com.example.yeongkkuel.presentation.home.store.data.SkinEquipItem
import com.example.yeongkkuel.presentation.home.store.data.SkinEquipRequest
import com.example.yeongkkuel.presentation.home.store.data.SkinEquipResponse
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException

class HomeRepository {
    private val api = RetrofitClient.homeApiService
    private val api2 = RetrofitClient.storeapiService

    private var isFetching = false // API 중복 요청 방지
    private var isFetchingReward = false // 리워드 API 중복 요청 방지

    suspend fun getShopData(itemType: String): ShopResponse? {
        return try {
            Log.d("HomeRepository", "🛒 API 요청: itemType = $itemType") // API 요청 로그 추가
            val response = api2.getShopData(itemType)

            if (response.isSuccessful) {
                Log.d("HomeRepository", "✅ API 응답 성공: ${response.body()}") // 응답 성공 로그
                response.body()
            } else {
                Log.e("HomeRepository", "❌ API 응답 실패: ${response.errorBody()?.string()}") // 응답 실패 로그
                null
            }
        } catch (e: Exception) {
            Log.e("HomeRepository", "❌ API 요청 중 오류 발생: ${e.message}") // 예외 발생 로그
            null
        }
    }
    suspend fun getHomeData(): HomeResult? {
        isFetching = false // 중복 요청 방지 해제

        return try {
            Log.d("HomeRepository", "홈 데이터 API 요청 시작")
            val response = api.getHomeData()

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Log.d("HomeRepository", "홈 데이터 응답: $body")

                    if (body.result == null) {
                        Log.e("HomeRepository", " 홈 데이터 응답의 `result`가 null! API 데이터 확인 필요")
                    }

                    return body.result
                } else {
                    Log.e("HomeRepository", "홈 데이터 응답 body가 null!")
                    return null
                }
            } else {
                Log.e("HomeRepository", "홈 데이터 요청 실패 - HTTP ${response.code()} : ${response.errorBody()?.string()}")
                return null
            }
        } catch (e: IOException) {
            Log.e("HomeRepository", "네트워크 오류: ${e.message}")
            return null
        } catch (e: HttpException) {
            Log.e("HomeRepository", " HTTP 오류 (코드: ${e.code()}): ${e.message}")
            return null
        } catch (e: Exception) {
            Log.e("HomeRepository", "알 수 없는 오류: ${e.message}")
            return null
        }
    }

    suspend fun saveEquippedSkins(purchaseIds: List<Int>): SkinEquipResponse? {
        return try {
            Log.d("HomeRepository", "스킨 착용 저장 요청 - purchaseIds: $purchaseIds")

            val requestBody = SkinEquipRequest(
                userItem = purchaseIds.map { SkinEquipItem(it) } // SkinEquipItem 리스트 변환
            )

            val response = api2.saveEquippedSkins(requestBody)

            if (response.isSuccessful) {
                response.body()?.also {
                    Log.d("HomeRepository", "스킨 착용 저장 성공: ${it.message}")

                    getHomeData() // 스킨 착용 후 홈 데이터를 다시 불러옴
                }
            } else {
                Log.e("HomeRepository", " 스킨 착용 저장 실패: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("HomeRepository", "스킨 착용 저장 API 오류: ${e.message}")
            null
        }
    }

    suspend fun getYesterdayReward(): RewardResponse? { // 어제 리워드 조회 추가
        if (isFetchingReward) {
            Log.w("HomeRepository", "리워드 API 요청 중, 중복 요청 방지됨")
            return null
        }

        isFetchingReward = true

        return try {
            Log.d("HomeRepository", " 어제 리워드 API 요청 시작")
            val response = api.getYesterdayReward()
            if (response.isSuccessful) {
                response.body() ?: run {
                    Log.e("HomeRepository", "리워드 응답이 null입니다.")
                    null
                }
            } else {
                Log.e("HomeRepository", "리워드 요청 실패: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: IOException) {
            Log.e("HomeRepository", "네트워크 오류: ${e.message}")
            null
        } catch (e: HttpException) {
            Log.e("HomeRepository", "HTTP 오류 (코드: ${e.code()}): ${e.message}")
            null
        } catch (e: Exception) {
            Log.e("HomeRepository", "알 수 없는 오류: ${e.message}")
            null
        } finally {
            isFetchingReward = false // API 요청 종료 후 false로 설정
        }
    }
}
