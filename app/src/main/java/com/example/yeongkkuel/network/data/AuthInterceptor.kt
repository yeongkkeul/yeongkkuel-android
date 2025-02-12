package com.example.yeongkkuel.network.data


import android.content.Context
import android.content.Intent
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import com.example.yeongkkuel.network.RetrofitClient
import com.example.yeongkkuel.presentation.auth.TokenManager
import com.example.yeongkkuel.network.request.login.ReissueRequest
import com.example.yeongkkuel.presentation.base.MainActivity
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

class AuthInterceptor(private val context: Context) : Interceptor {

    @Volatile
    private var isRefreshing: Boolean = false

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 1) 저장된 AccessToken 가져오기
        val accessToken = TokenManager.getAccessToken(context).orEmpty()

        // 2) 토큰이 있다면 Header에 추가
        val requestWithAccessToken = if (accessToken.isNotEmpty()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        } else {
            originalRequest
        }

        // 3) 요청 실행
        val response = chain.proceed(requestWithAccessToken)

        // 4) 만약 첫 응답이 401 → 토큰 재발급 시도
        if (response.code == 401) {
            // 기존 response는 더이상 안 쓰므로 닫아준다(자원 해제)
            response.close()

            // 이미 다른 스레드/요청에서 토큰 재발급 중이면, 추가 시도 없이 바로 401 응답
            if (isRefreshing) {
                return response
            }

            synchronized(this) {
                if (!isRefreshing) {
                    isRefreshing = true
                    val refreshSuccess = tryRefreshToken()
                    isRefreshing = false

                    // 재발급 실패하면 → 토큰 삭제(로그아웃 처리) 후 그대로 401 반환
                    if (!refreshSuccess) {
                        TokenManager.clearTokens(context)
                        return response
                    }
                }
            }

            // 여기 도달했다면 “재발급 성공”한 상태
            // 새 토큰으로 한 번 더 원본 요청을 실행
            val newAccessToken = TokenManager.getAccessToken(context).orEmpty()
            val refreshedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .build()

            val secondResponse = chain.proceed(refreshedRequest)

            // ★ 재발급 후 재시도했는데도 또 401이면 → 토큰 삭제 후 401 반환
            if (secondResponse.code == 401) {
                secondResponse.close()
                TokenManager.clearTokens(context)
                // 실제 로그아웃 처리나 사용자 세션 만료 안내 등의 추가 로직을 여기에 넣으면 됨 로그아웃 처리하자
                // activity 재실행
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            }

            return secondResponse
        }

        // 401이 아니면 그냥 기존 응답 반환
        return response
    }

    /**
     * 실제 토큰 재발급 로직
     * - 성공 시 true, 실패 시 false
     */
    private fun tryRefreshToken(): Boolean {
        return try {
            runBlocking {
                val oldAccessToken = TokenManager.getAccessToken(context).orEmpty()
                val oldRefreshToken = TokenManager.getRefreshToken(context).orEmpty()

                val refreshReq = ReissueRequest(
                    accessToken = oldAccessToken,
                    refreshToken = oldRefreshToken
                )
                val call = RetrofitClient.reissueApiService.reissueToken(refreshReq)
                val retrofitResponse = call.execute()

                if (retrofitResponse.isSuccessful && retrofitResponse.body() != null) {
                    val newTokens = retrofitResponse.body()!!
                    TokenManager.saveTokens(context, newTokens.accessToken, newTokens.refreshToken)
                    Timber.d("Token refreshed successfully: ${newTokens.accessToken}")
                    true
                } else {
                    Timber.e("Token refresh failed with code: ${retrofitResponse.code()}")
                    false
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Token refresh error")
            false
        }
    }
}
