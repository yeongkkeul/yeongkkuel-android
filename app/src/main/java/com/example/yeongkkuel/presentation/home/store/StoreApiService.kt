package com.example.yeongkkuel.presentation.home.store

import com.example.yeongkkuel.presentation.home.store.data.ShopResponse
import com.example.yeongkkuel.presentation.home.store.data.SkinEquipRequest
import com.example.yeongkkuel.presentation.home.store.data.SkinEquipResponse
import com.example.yeongkkuel.presentation.home.store.data.SkinPurchaseRequest
import com.example.yeongkkuel.presentation.home.store.data.SkinPurchaseResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface StoreApiService {
    @PUT("api")// 스킨 착용 저장 API
    suspend fun saveEquippedSkins(@Body request: SkinEquipRequest): Response<SkinEquipResponse>

    @POST("api") // 스킨 구매 API
    suspend fun purchaseSkin(@Body request: SkinPurchaseRequest): Response<SkinPurchaseResponse>

    @GET("api/shop/") // 착용 스킨 + 보유 리워드 + 상점 뷰 API 추가
    suspend fun getShopData(@Query("itemType") itemType: String): Response<ShopResponse>
}