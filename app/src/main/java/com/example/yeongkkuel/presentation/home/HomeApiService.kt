package com.example.yeongkkuel.presentation.home

import retrofit2.Response // ✅ 올바른 Response import
import retrofit2.http.GET

interface HomeApiService {
    @GET("api/home") // ✅ 홈 화면 데이터 조회 API
    suspend fun getHomeData(): Response<HomeResponse>
}
