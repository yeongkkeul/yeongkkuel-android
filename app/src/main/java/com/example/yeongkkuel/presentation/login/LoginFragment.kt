package com.example.yeongkkuel.presentation.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.yeongkkuel.BuildConfig
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentLoginBinding
import com.example.yeongkkuel.presentation.auth.TokenManager
import com.example.yeongkkuel.presentation.login.response.KakaoLoginResponse
import com.example.yeongkkuel.network.RetrofitClient
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.common.model.AuthError
import com.kakao.sdk.common.model.AuthErrorCause
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

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
            signInWithGoogle()
//            findNavController().navigate(R.id.action_loginFragment_to_navigation_home)

        }
    }


    private fun signInWithGoogle() {
        // BeginSignInRequest 빌드
        val credentialManager = CredentialManager.create(requireContext())
        val googleClientID: String = BuildConfig.google_CLIENT_ID
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)   // 구글 계정 유무 체크
            .setServerClientId(googleClientID)  // 웹 클라이언트 키값
            .setAutoSelectEnabled(true)            // 자동 로그인 활성화
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        CoroutineScope(Dispatchers.Main).launch {
            runCatching {
                val result = credentialManager.getCredential(requireContext(), request)

                // Credential 처리
                when (val data = result.credential) {
                    is CustomCredential -> {
                        if (data.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(data.data)

                            val idToken = googleIdTokenCredential.idToken
                            Timber.e("ID Token: $idToken")

                            // ID 토큰 전달 후 처리
                            fetchUserInfoWithGoogle(idToken)
                        }
                    }
                }
            }.onFailure { error ->
                Timber.e("Google Login Error: $error")
            }
        }
    }

    private fun fetchUserInfoWithGoogle(idToken: String?) {
        if (idToken == null) {
            Timber.e("ID Token is null, cannot proceed to fetch user info")
            return
        }

        // 백엔드로 ID 토큰 전송
        CoroutineScope(Dispatchers.IO).launch {
            val success = postIdTokenToBackend(idToken) // ID 토큰을 서버로 전송하고 성공 여부 반환
            withContext(Dispatchers.Main) {
                if (success) {
                    // 성공 시 회원가입 페이지로 이동 - redirect Url 에 따라 분기
                    findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
                } else {
                    // 실패 처리
                    Timber.e("Failed to verify ID Token with backend")
                }
            }
        }
    }

    // TODO: 서버와 통신하여 ID 토큰 검증 (Retrofit 구현)
    private suspend fun postIdTokenToBackend(idToken: String): Boolean {
        return true

        /*try {
            // 서버 API 호출 (예: Retrofit)
            val response = apiService.verifyGoogleIdToken(idToken) // 서버 검증 엔드포인트 호출
            response.isSuccessful
        } catch (e: Exception) {
            Timber.e("Error while verifying ID Token: $e")
            false
        }*/
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
                postKakaoTokenToBackend(token.accessToken)
            }
        }
    }


    private fun loginWithKakaoAccount() {
        UserApiClient.instance.loginWithKakaoAccount(requireContext()) { token, error ->
            if (error != null) {
                handleLoginError(error)
            } else if (token != null) {
                Timber.tag("KakaoLogin").i("카카오 계정 로그인 성공. 토큰 정보: ${token.accessToken}")
                postKakaoTokenToBackend(token.accessToken)
            }
        }

    }

    private fun postKakaoTokenToBackend(kakaoAccessToken: String) {
        // accesstoken 확인
        Timber.tag("KakaoLogin").i("카카오 로그인 성공. 토큰 정보: $kakaoAccessToken")

        RetrofitClient.loginApiService.kakaoLogin(kakaoAccessToken)
            .enqueue(object : Callback<KakaoLoginResponse> {
                override fun onResponse(
                    call: Call<KakaoLoginResponse>,
                    response: Response<KakaoLoginResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.isSuccess == true) {
                            val result = body.result
                            if (result != null) {
                                // JWT 저장
                                TokenManager.saveTokens(requireContext(), result.accessToken, result.refreshToken)
                                /*tokenManager.saveAccessToken(result.accessToken)
                                tokenManager.saveRefreshToken(result.refreshToken)
*/
                                // redirectUrl에 따라 분기
                                when (result.redirectUrl) {
                                    "/api/home" -> {
                                        // 이미 회원 -> 홈으로 이동
                                        findNavController().navigate(R.id.action_loginFragment_to_navigation_home)
                                    }
                                    "/api/auth/user-info" -> {
                                        // 회원 기입 필요 -> 회원가입 flow로 이동
                                        findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
                                    }
                                    else -> {
                                        // 기타 URL인 경우? 필요 시 처리
                                        Toast.makeText(requireContext(), "알 수 없는 리디렉션", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(requireContext(), "로그인 응답에 result가 없습니다.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // isSuccess=false or 응답 코드가 다른 경우 => 실패 처리
                            Toast.makeText(requireContext(), "카카오 로그인 실패", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // HTTP 4xx/5xx
                        Toast.makeText(requireContext(), "카카오 로그인 API 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<KakaoLoginResponse>, t: Throwable) {
                    Timber.e("카카오 로그인 API 호출 실패: $t")
                    Toast.makeText(requireContext(), "카카오 로그인 API 호출 실패: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

   /* // 사용자 정보 요청 - 사용자 요청 정보 전달.
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
    }*/

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
}