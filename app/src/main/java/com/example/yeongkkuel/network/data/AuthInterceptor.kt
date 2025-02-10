package com.example.yeongkkuel.network.data


import android.content.Context
import com.example.yeongkkuel.network.RetrofitClient
import com.example.yeongkkuel.presentation.auth.TokenManager
import com.example.yeongkkuel.network.request.login.ReissueRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // 원본 요청
        val originalRequest = chain.request()

        // 저장된 AccessToken 가져오기
        val accessToken = TokenManager.getAccessToken(context) ?: ""
        val refreshToken = TokenManager.getRefreshToken(context)?: ""

        // 토큰이 있다면 헤더에 추가
        val newRequest = if (!accessToken.isNullOrEmpty()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(newRequest)
    }
}