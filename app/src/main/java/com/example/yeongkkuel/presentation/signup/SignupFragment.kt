package com.example.yeongkkuel.presentation.signup

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentSignupBinding // 자동 생성된 바인딩 클래스
import com.example.yeongkkuel.network.request.login.ReferralRequest
import com.example.yeongkkuel.network.request.login.UserInfoRequest
import com.example.yeongkkuel.network.RetrofitClient
import kotlinx.coroutines.launch
import timber.log.Timber

class SignupFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    // 선택된 항목 (없으면 null)
    private var selectedGender: View? = null
    private var selectedAge: View? = null
    private var selectedJob: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        val rootView = binding.root

        // 뒤로가기
        binding.ivBack.setOnClickListener { findNavController().navigateUp() }

        // 닉네임 입력 글자 수 검증
        setupNicknameValidation()

        // 성별/나이/직업 단일 선택 설정
        setupSelectableViews()

        // 완료 버튼
        binding.btnSignupComplete.setOnClickListener { validateAndSubmit() }

        return rootView
    }

    private fun setupNicknameValidation() {
        binding.etNickname.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 최대 10자 제한
                if (s != null && s.length > 10) {
                    binding.etNickname.setText(s.subSequence(0, 10))
                    binding.etNickname.setSelection(10)
                }
                // 글자 수 표시
                binding.tvCharCount.text = "${s?.length ?: 0}/10"

                // 에러 UI 초기화
                binding.clNickname.setBackgroundResource(R.drawable.rounded_nickname_background)
                binding.tvNicknameError.visibility = View.INVISIBLE
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupSelectableViews() {
        // 각각의 GridLayout(또는 ViewGroup)에 대해 단일 선택 처리
        setupSingleSelection(binding.glGenderGroup) { view, isSelected ->
            // 클릭 후에 selectedGender 를 설정 / 해제
            selectedGender = if (isSelected) view else null
        }
        setupSingleSelection(binding.glAgeGroup) { view, isSelected ->
            selectedAge = if (isSelected) view else null
        }
        setupSingleSelection(binding.glJobGroup) { view, isSelected ->
            selectedJob = if (isSelected) view else null
        }
    }

    /**
     * 단일 선택이지만, 이미 선택된 뷰를 다시 탭하면 '해제' 가능한 로직
     */
    private fun setupSingleSelection(group: ViewGroup, onSelectionChanged: (View?, Boolean) -> Unit) {
        for (i in 0 until group.childCount) {
            val child = group.getChildAt(i)
            child.setOnClickListener {
                if (child.isSelected) {
                    // 이미 선택된 항목을 다시 누르면 -> 해제
                    child.isSelected = false
                    (child as? TextView)?.apply {
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                        setTextAppearance(R.style.body_regula)
                    }
                    onSelectionChanged(child, false)
                } else {
                    // 새로운 항목을 누르면, 기존 모두 해제
                    for (j in 0 until group.childCount) {
                        val sibling = group.getChildAt(j)
                        sibling.isSelected = false
                        (sibling as? TextView)?.apply {
                            setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                            setTextAppearance(R.style.body_regula)
                        }
                    }
                    // 현재 뷰 선택
                    child.isSelected = true
                    (child as? TextView)?.apply {
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.main1))
                        setTextAppearance(R.style.body_semibo)
                    }
                    onSelectionChanged(child, true)
                }
            }
        }
    }

    private fun validateAndSubmit() {
        val nickname = binding.etNickname.text.toString().trim()

        // 닉네임 검증
        if (nickname.isEmpty()) {
            binding.clNickname.setBackgroundResource(R.drawable.rounded_nickname_background_error)
            binding.tvNicknameError.visibility = View.VISIBLE
            return
        }

        // 성별/나이/직업 텍스트 추출 (없으면 "")
        val genderText = (selectedGender as? TextView)?.text?.toString() ?: ""
        val ageText = (selectedAge as? TextView)?.text?.toString() ?: ""
        val jobText = (selectedJob as? TextView)?.text?.toString() ?: ""

        // 서버 전송용으로 매핑
        val serverGender = mapGenderToServer(genderText)
        val serverAgeGroup = mapAgeToServer(ageText)
        val serverJob = mapJobToServer(jobText)

        sendSignupDataToBackend(nickname, serverGender, serverAgeGroup, serverJob)
    }

    private fun sendSignupDataToBackend(nickname: String, gender: String, ageGroup: String, job: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val request = UserInfoRequest(
                    nickName = nickname,
                    gender = gender,
                    ageGroup = ageGroup,
                    job = job
                )
                // 통신
                val response = RetrofitClient.loginApiService.postUserInfo(request)

                if (response.isSuccess) {
                    // 성공 시 추천인 코드 입력 다이얼로그
                    showRecommendCodeDialog()
                } else {
                    // 실패 시 에러 메시지
                    Toast.makeText(requireContext(), "가입 실패: ${response.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showRecommendCodeDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        val params = dialog.window?.attributes
        params?.dimAmount = 0.65f
        dialog.window?.attributes = params
        dialog.setContentView(R.layout.dialog_recommend_code)
        dialog.setCancelable(false)

        val etRecommendCode = dialog.findViewById<TextView>(R.id.et_recommend_code)
        val btnSkip = dialog.findViewById<View>(R.id.tv_skip)
        val btnConfirm = dialog.findViewById<View>(R.id.tv_recommend_close)
        val errorTextView = dialog.findViewById<TextView>(R.id.tv_recommend_code_error)

        // 건너뛰기
        btnSkip.setOnClickListener {
            dialog.dismiss()
            navigateToTermsAgree(showRewardModal = false)
        }

        // 확인
        btnConfirm.setOnClickListener {
            val code = etRecommendCode.text.toString().trim()

            if (code.isEmpty()) {
                errorTextView.visibility = View.VISIBLE
                errorTextView.text = "코드를 입력해주세요."
                return@setOnClickListener
            }
            if (code.length != 6) {
                errorTextView.visibility = View.VISIBLE
                errorTextView.text = "코드는 6자리여야 합니다."
                return@setOnClickListener
            }

            verifyReferralCodeApi(code, dialog, errorTextView)
        }
    }

    // 초대 코드 검증
    private fun verifyReferralCodeApi(referralCode: String, dialog: Dialog, errorTextView: TextView) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val request = ReferralRequest(referralCode)
                val response = RetrofitClient.loginApiService.validateRecommendCode(request)

                if (!response.isSuccess) {
                    // 서버 응답 자체가 실패
                    errorTextView.visibility = View.VISIBLE
                    errorTextView.text = response.message
                } else {
                    // 코드 유효 → 다음 화면 이동
                    dialog.dismiss()
                    navigateToTermsAgree(showRewardModal = true)
                }

            } catch (e: Exception) {
                // 네트워크 장애, 예외 발생 등
                if (e.message?.contains("404") == true) {
                    errorTextView.visibility = View.VISIBLE
                    errorTextView.text = "존재하지 않는 추천인 코드입니다."
                } else {
                    errorTextView.visibility = View.VISIBLE
                    errorTextView.text = "오류가 발생했습니다: ${e.message}"
                }
            }
        }
    }

    private fun navigateToTermsAgree(showRewardModal: Boolean) {
        val bundle = Bundle()
        bundle.putBoolean("showRewardModal", showRewardModal)
        findNavController().navigate(R.id.action_signupFragment_to_navigation_terms_agree, bundle)
    }

    private fun mapGenderToServer(uiText: String): String {
        return when (uiText) {
            "남자" -> "Male"
            "여자" -> "Female"
            else -> "UNDECIDED"  // 아무것도 선택 안 했거나, 기타
        }
    }
    private fun mapAgeToServer(uiText: String): String {
        return when (uiText) {
            "14~19세" -> "TEENAGER"
            "20대" -> "TWENTIES"
            "30대" -> "THIRTIES"
            "40대" -> "FORTIES"
            "50대" -> "FIFTIES"
            "60대 이상" -> "SIXTIES_AND_ABOVE"
            else -> "UNDECIDED"
        }
    }
    private fun mapJobToServer(uiText: String): String {
        return when (uiText) {
            "학생" -> "STUDENT"
            "직장인" -> "EMPLOYEE"
            "주부" -> "HOMEMAKER"
            "자영업자" -> "SELF_EMPLOYED"
            else -> "UNDECIDED"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
