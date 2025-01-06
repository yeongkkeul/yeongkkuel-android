package com.example.yeongkkuel.presentation.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentLoginBinding
import com.kakao.sdk.common.model.AuthError
import com.kakao.sdk.common.model.AuthErrorCause
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import timber.log.Timber

// local.properties 파일에 저장된 native_app_key를 가져옴


class LoginFragment : Fragment() {



    private var _binding: FragmentLoginBinding? = null
    private val binding: FragmentLoginBinding

        get() = requireNotNull(_binding) { "FragmentLoginBinding -> null" }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("LoginFragment", "onViewCreated")

        binding.btnKakaoLogin.setOnClickListener {
            handleKakaoLogin()
        }

        binding.btnGoogleLogin.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_navigation_home)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun handleKakaoLogin() {
        // 카카오톡 앱 설치 여부 확인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(requireContext())) {
            // 카카오톡으로 로그인
            loginWithKakaoTalk()
        } else {
            // 카카오 계정으로 로그인
            loginWithKakaoAccount()
        }
    }


    private fun loginWithKakaoTalk() {
        UserApiClient.instance.loginWithKakaoTalk(requireContext()) { token, error ->
            if (error != null) {
                handleLoginError(error)
            } else if (token != null) {
                Timber.tag("KakaoLogin").i("카카오톡 로그인 성공. 토큰 정보: ${token.accessToken}")
                fetchUserInfo(token.accessToken)
            }
        }
    }

    private fun loginWithKakaoAccount() {
        UserApiClient.instance.loginWithKakaoAccount(requireContext()) { token, error ->
            if (error != null) {
                handleLoginError(error)
            } else if (token != null) {
                Timber.tag("KakaoLogin").i("카카오 계정 로그인 성공. 토큰 정보: ${token.accessToken}")
                fetchUserInfo(token.accessToken)
            }
        }
    }

    private fun fetchUserInfo(accessToken: String) {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Timber.tag("KakaoLogin").e(error, "사용자 정보 요청 실패")
                Toast.makeText(requireContext(), "사용자 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
            } else if (user != null) {
                Timber.tag("KakaoLogin").i("사용자 정보 요청 성공: ${user}")
                val id = user.id
                val nickname = user.kakaoAccount?.profile?.nickname
                val email = user.kakaoAccount?.email
                val profileImage = user.kakaoAccount?.profile?.profileImageUrl

                // 사용자 정보를 활용한 로직
                Toast.makeText(
                    requireContext(),
                    "환영합니다, ${nickname}님!",
                    Toast.LENGTH_SHORT
                ).show()

                // 다음 화면으로 이동 예시
                navigateToSignUp()
            }
        }
    }

    private fun handleLoginError(error: Throwable) {
        when (error) {
            is ClientError -> {
                if (error.reason == ClientErrorCause.Cancelled) {
                    Timber.tag("KakaoLogin").e("사용자가 로그인 취소")
                } else {
                    Timber.tag("KakaoLogin").e("클라이언트 에러 발생: ${error.reason}")
                }
            }
            is AuthError -> {
                when (error.reason) {
                    AuthErrorCause.AccessDenied -> Timber.tag("KakaoLogin").e("액세스가 거부되었습니다")
                    AuthErrorCause.InvalidClient -> Timber.tag("KakaoLogin").e("유효하지 않은 클라이언트입니다")
                    AuthErrorCause.InvalidGrant -> Timber.tag("KakaoLogin").e("잘못된 인증 코드입니다")
                    AuthErrorCause.InvalidRequest -> Timber.tag("KakaoLogin").e("유효하지 않은 요청입니다")
                    AuthErrorCause.InvalidScope -> Timber.tag("KakaoLogin").e("유효하지 않은 범위입니다")
                    AuthErrorCause.Misconfigured -> Timber.tag("KakaoLogin").e("설정 오류")
                    AuthErrorCause.ServerError -> Timber.tag("KakaoLogin").e("서버 오류 발생")
                    AuthErrorCause.Unauthorized -> Timber.tag("KakaoLogin").e("권한이 없습니다")
                    else -> Timber.tag("KakaoLogin").e("알 수 없는 인증 오류")
                }
            }
            else -> {
                Timber.tag("KakaoLogin").e("로그인 실패: $error")
                Toast.makeText(requireContext(), "로그인 실패: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToSignUp() {
        // 회원가입 화면으로 이동
        // 예: Navigation Component를 사용한 화면 전환
        // findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
    }
}