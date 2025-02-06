package com.example.yeongkkuel.network.data


import android.content.Context
import com.example.yeongkkuel.presentation.auth.ReissueApiService
import com.example.yeongkkuel.presentation.auth.TokenManager
import com.example.yeongkkuel.presentation.login.request.ReissueRequest
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import timber.log.Timber

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // 원본 요청
        val originalRequest = chain.request()

        // 저장된 AccessToken 가져오기
        val accessToken = TokenManager.getAccessToken(context)

        // 토큰이 있다면 헤더에 추가
        val newRequest = if (!accessToken.isNullOrEmpty()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        } else {
            originalRequest
        }

        // 요청 진행
        return chain.proceed(newRequest)
    }
}