package com.example.yeongkkuel.presentation.network.data


import com.example.yeongkkuel.presentation.auth.ReissueApiService
import com.example.yeongkkuel.presentation.auth.TokenManager
import com.example.yeongkkuel.presentation.login.request.ReissueRequest
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit

class AuthInterceptor(
    private val tokenManager: TokenManager,
    private val baseRetrofit: Retrofit
) : Interceptor {
    @Volatile
    private var isRefreshing = false

    override fun intercept(chain: Interceptor.Chain): Response {
        // 1) 기존 요청
        var request = chain.request()

        // 2) 토큰이 있다면 헤더 추가
        val accessToken = tokenManager.getAccessToken()
        if (!accessToken.isNullOrBlank()) {
            request = newRequestWithAccessToken(request, accessToken)
        }

        // 3) 요청 실행
        val response = chain.proceed(request)

        // 4) 401(Unauthorized) 시 → 토큰 재발급 로직
        if (response.code == 401) {
            response.close() // 기존 response 닫기 (안 닫으면 메모리 누수 가능)

            // 재발급 로직 (동시성 방지)
            synchronized(this) {
                if (!isRefreshing) {
                    isRefreshing = true

                    val success = reissueToken()
                    isRefreshing = false

                    if (!success) {
                        // 재발급 실패 -> 토큰 제거 or 로그아웃 처리
                        tokenManager.clearTokens()
                        // 필요 시 여기서 return response (실패 처리) 하거나 throw Exception
                        return chain.proceed(request)
                    }
                }
            }
            // 재발급에 성공했다면 다시 한번 요청 재시도
            val newToken = tokenManager.getAccessToken()
            val newRequest = newRequestWithAccessToken(request, newToken)
            return chain.proceed(newRequest)
        }
        return response
    }

    private fun newRequestWithAccessToken(originalRequest: Request, accessToken: String?): Request {
        return originalRequest.newBuilder()
            .removeHeader("Authorization")
            .addHeader("Authorization", "Bearer $accessToken")
            .build()
    }

    /**
     * /api/reissue 호출 -> 성공 시 새 토큰 저장 -> true 반환
     */
    private fun reissueToken(): Boolean {
        val oldAccessToken = tokenManager.getAccessToken() ?: return false
        val refreshToken = tokenManager.getRefreshToken() ?: return false

        val reissueApi = baseRetrofit.create(ReissueApiService::class.java)
        val requestBody = ReissueRequest(oldAccessToken, refreshToken)
        return try {
            val response = reissueApi.reissueToken(requestBody).execute()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    // 새 accessToken/refreshToken 저장
                    tokenManager.saveAccessToken(body.accessToken)
                    tokenManager.saveRefreshToken(body.refreshToken)
                    true
                } else false
            } else false
        } catch (e: Exception) {
            false
        }
    }
}