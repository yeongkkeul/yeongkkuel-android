package com.example.yeongkkuel.presentation.splash

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.auth0.android.jwt.JWT
import com.example.yeongkkuel.R
import com.example.yeongkkuel.network.RetrofitClient
import com.example.yeongkkuel.presentation.auth.TokenManager
import com.example.yeongkkuel.network.request.login.ReissueRequest
import kotlinx.coroutines.*

class SplashFragment : Fragment() {



    private var job: Job? = null // 코루틴 작업 관리

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("SplashFragment", "onViewCreated")

        val logo = view.findViewById<ImageView>(R.id.iv_logo)

        // 로고 애니메이션 실행
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        logo.startAnimation(slideUp)

        // 애니메이션 완료 후 LoginFragment로 이동
        logo.postDelayed({

            lifecycleScope.launch {
                val isLoggedIn = autoLogin(requireContext(), TokenManager)
                if (isLoggedIn) {
                    // 자동 로그인 성공: Splash → HomeFragment로 이동
                    findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
                } else {
                    // 자동 로그인 실패: Splash → LoginFragment로 이동 (이후 LoginFragment에서 정상 로그인 시 Home으로 이동)
                    findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
                }
            }
        }, 1000) // 애니메이션 지속 시간과 동일하게 설정


    }


    suspend fun autoLogin(context: Context, tokenManager: TokenManager): Boolean {
        // SharedPreferences에서 토큰을 가져옴 (앱의 이름이나 PREF_KEY는 상황에 맞게 설정)
        var accessToken = tokenManager.getAccessToken(context)?: null
        val refreshToken = tokenManager.getRefreshToken(context)?: null

        // 토큰이 없으면 자동 로그인 실패
        if (accessToken.isNullOrEmpty() || refreshToken.isNullOrEmpty()) {
            return false
        }

        return try {
            // JWT 라이브러리를 사용하여 access token 파싱
            val jwt = JWT(accessToken)
            // jwt.isExpired() 메서드는 기본적으로 현재 시간과 비교하여 만료 여부를 체크합니다.
            if (jwt.isExpired(0)) {
                // access token이 만료되었으므로 refresh token을 이용하여 토큰 갱신 시도
                try {
                    // 네트워크 호출은 IO 스레드에서 수행
                    val response = withContext(Dispatchers.IO) {
                        RetrofitClient.reissueApiService
                            .reissueToken(ReissueRequest(accessToken, refreshToken))
                            .execute()
                    }
                    // 갱신 성공: 새 토큰을 SharedPreferences에 저장
                    if (response.isSuccessful && response.body() != null) {
                        val newAccessToken = response.body()!!.accessToken
                        val newRefreshToken = response.body()!!.refreshToken
                        if (newAccessToken.isNotEmpty()) {
                            tokenManager.saveTokens(context, newAccessToken, refreshToken)
                        }
                    }
                    true  // 자동 로그인 성공
                } catch (e: Exception) {
                    // 네트워크 오류 등 기타 예외
                    e.printStackTrace()
                    tokenManager.clearTokens(context)
                    false
                } finally {
                    // 자동 로그인 실패 시 로그아웃 처리
                    if (!true) {
                        tokenManager.clearTokens(context)
                    }
                }
            } else {
                // access token이 유효한 경우 바로 자동 로그인 성공
                true
            }
        } catch (e: Exception) {
            // 토큰 디코딩 중 오류가 발생한 경우 (예: 변조된 토큰)
            tokenManager.clearTokens(context)
            e.printStackTrace()
            false
        }
    }


}