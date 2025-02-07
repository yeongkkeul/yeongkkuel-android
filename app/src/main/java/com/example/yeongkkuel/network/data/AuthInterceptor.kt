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

        val response = chain.proceed(newRequest)

        if(response.code == 401) {
            // 리프레시 토큰을 가져와서 , 갱신 요청을 한다.
            var refreshedAccessToken: String
            var refreshedRefreshToken: String

            runBlocking {
                val refreshTokenRequest = ReissueRequest(accessToken, refreshToken)
                val refreshTokenResponse =
                    RetrofitClient.reissueApiService.reissueToken(refreshTokenRequest)
                        .execute().body()!!

                // 토큰 갱신 성공  및 저장
                refreshedAccessToken = refreshTokenResponse.accessToken
                refreshedRefreshToken = refreshTokenResponse.refreshToken
                TokenManager.saveTokens(context, refreshedAccessToken, refreshedRefreshToken)
                Timber.d("Token refreshed successfully: new access token = $refreshedAccessToken")


            }
            val refreshedRequest = chain.request().newBuilder()
                .header("Authorization","Bearer $refreshedAccessToken")
                .build()
            return chain.proceed(refreshedRequest)
        }

        // 요청 진행
        return response
    }
}