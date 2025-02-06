package com.example.yeongkkuel.presentation.home

import android.util.Log
import com.example.yeongkkuel.presentation.network.RetrofitClient

class HomeRepository {
    private val api = RetrofitClient.homeApiService

    suspend fun getHomeData(): HomeResponse? {
        return try {
            val response = api.getHomeData()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            Log.e("HomeRepository", "홈 데이터 조회 오류: ${e.message}")
            null
        }
    }
}