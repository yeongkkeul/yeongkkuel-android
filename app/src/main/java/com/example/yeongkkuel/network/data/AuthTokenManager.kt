package com.example.yeongkkuel.network.data

import android.content.Context
import com.example.yeongkkuel.network.RetrofitClient
import com.example.yeongkkuel.network.request.login.ReissueRequest
import com.example.yeongkkuel.presentation.auth.TokenManager


class AuthTokenManager(
    private val context: Context
) {
    private val reissueService = RetrofitClient.reissueApiService

    suspend fun refreshAccessToken(): String? {
        // 저장된 refreshToken 가져오기
        val accessToken = TokenManager.getAccessToken(context) ?: null
        val refreshToken = TokenManager.getRefreshToken(context) ?: null

        // refreshToken이 없으면 갱신 불가능
        if (refreshToken.isNullOrEmpty() || accessToken.isNullOrEmpty()) return null

        return try {
            // 갱신 API 호출
            val response = reissueService.reissueToken(ReissueRequest(accessToken, refreshToken)).execute().body()
            if(response != null) {
                val refreshedAccessToken = response.accessToken
                val refreshedRefreshToken = response.refreshToken
                TokenManager.saveTokens(context, refreshedAccessToken, refreshedRefreshToken)

                TokenManager.getAccessToken(context)
            } else {
                TokenManager.clearTokens(context)
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            TokenManager.clearTokens(context)
            null
        }
    }
}
