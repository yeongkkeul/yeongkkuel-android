package com.example.yeongkkuel.presentation.home

import android.util.Log
import com.example.yeongkkuel.network.RetrofitClient
import retrofit2.HttpException
import java.io.IOException

class HomeRepository {
    private val api = RetrofitClient.homeApiService
    private var isFetching = false // API 중복 요청 방지
    private var isFetchingReward = false // 리워드 API 중복 요청 방지

    suspend fun getHomeData(): HomeResult? { // ✅ 반환 타입 변경
        if (isFetching) {
            return null
        }

        isFetching = true // ✅ API 요청 시작 전 true 설정

        return try {
            Log.d("HomeRepository", "🚀 홈 데이터 API 요청 시작")
            val response = api.getHomeData()
            if (response.isSuccessful) {
                response.body()?.result ?: run {
                    Log.e("HomeRepository", "홈 데이터 응답의 result가 null입니다.")
                    null
                }
            } else {
                Log.e("HomeRepository", "홈 데이터 요청 실패: ${response.errorBody()?.string()}")
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
            isFetching = false // API 요청 종료 후 false로 설정
        }
    }

    suspend fun getYesterdayReward(): RewardResponse? { // ✅ 어제 리워드 조회 추가
        if (isFetchingReward) {
            Log.w("HomeRepository", "⚠️ 리워드 API 요청 중, 중복 요청 방지됨")
            return null
        }

        isFetchingReward = true

        return try {
            Log.d("HomeRepository", "🚀 어제 리워드 API 요청 시작")
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
