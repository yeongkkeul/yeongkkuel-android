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
import com.kakao.sdk.auth.AuthApiClient
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



        // 카카오 버튼 클릭 시
        binding.btnKakaoLogin.setOnClickListener {
            //이미 인가 코드를 받았다면 로그아웃하는 로직 추가
            if (AuthApiClient.instance.hasToken()) {
                // 이미 세션이 살아있는 경우
                Timber.d("이미 카카오 로그인이 되어 있습니다. 재로그인을 위해 로그아웃 시도.")
                logoutAndReLogin()
            } else {
                // 세션이 없으므로 기존대로 로그인 진행
                handleKakaoLogin()
            }
        }

        // TODO: 구글 로그인 구현
        binding.btnGoogleLogin.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_navigation_home)
        }
    }

    private fun logoutAndReLogin() {
        UserApiClient.instance.logout { error ->
            if (error != null) {
                Timber.e("카카오 로그아웃 실패: $error")
                // 그래도 로그인은 시도해볼 수 있음
                handleKakaoLogin()
            } else {
                Timber.d("카카오 로그아웃 성공. 이제 다시 로그인 시도.")
                handleKakaoLogin()
            }
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

    // 사용자 정보 요청 - 사용자 요청 정보 전달.
    private fun fetchUserInfo(accessToken: String) {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Timber.tag("KakaoLogin").e(error, "사용자 정보 요청 실패")
                Toast.makeText(requireContext(), "사용자 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
            } else if (user != null) {
                Timber.tag("KakaoLogin").i("사용자 정보 요청 성공: ${user}")
                val nickname = user.kakaoAccount?.profile?.nickname
                Toast.makeText(
                    requireContext(),
                    "환영합니다, ${nickname}님!",
                    Toast.LENGTH_SHORT
                ).show()

                // 백엔드에 인가 코드 전달하기 -TODO: 백엔드에 인가 코드 전달하는 방법 고민

                // 응답으로 성공 or 실패를 받음.
                //실패로직과 성공로직으로 나눔. -TODO: 실패로직과 성공로직 구현

                // 성공 시 - 다음 화면으로 이동
                navigateToSignUp()
                // 실패 시 - 실패 메시지 출력
            }
        }
    }

    // 로그인 실패 시 에러 처리 - 로그인 실패 시 로그만 띄우기? - TODO: 실패 시 처리 방법 고민
    private fun handleLoginError(error: Throwable) {
        when (error) {
            is ClientError -> {
                if (error.reason == ClientErrorCause.Cancelled) {
                    // 사용자가 로그인 취소 시
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
                    else -> Timber.tag("KakaoLogin").e("알 수 없는 인증 오류 ${error.reason}")
                }
            }
            else -> {
                Timber.tag("KakaoLogin").e("로그인 실패: $error")
                Toast.makeText(requireContext(), "로그인 실패: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun navigateToSignUp() {
        findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
    }
}