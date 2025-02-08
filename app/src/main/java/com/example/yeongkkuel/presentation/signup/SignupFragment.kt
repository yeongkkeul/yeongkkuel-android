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

    // View Binding
    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!


    private var selectedGender: View? = null
    private var selectedAge: View? = null
    private var selectedJob: View? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // 바인딩 객체 초기화
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        val rootView = binding.root
        // ic back 클릭 시 뒤로가기
        binding.ivBack.setOnClickListener { findNavController().navigateUp() }
        // 입력 값 검증
        setupNicknameValidation()
        // 그룹에 대한 선택 처리
        setupSelectableViews()

        // 완료 버튼
        binding.btnSignupComplete.setOnClickListener { validateAndSubmit() }

        return rootView
    }

    private fun setupNicknameValidation() {
        // etNickname: 바인딩 객체를 통해 접근
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

                // 입력 중 에러 메시지 및 테두리 초기화
                binding.clNickname.setBackgroundResource(R.drawable.rounded_nickname_background)
                binding.tvNicknameError.visibility = View.INVISIBLE
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupSelectableViews() {
        // GridLayout 등을 바인딩으로 접근
        val genderGroup = binding.glGenderGroup
        val ageGroup = binding.glAgeGroup
        val jobGroup = binding.glJobGroup

        setupSingleSelection(genderGroup)
        setupSingleSelection(ageGroup)
        setupSingleSelection(jobGroup)
    }

    private fun setupSingleSelection(group: ViewGroup) {
        for (i in 0 until group.childCount) {
            val child = group.getChildAt(i)
            child.setOnClickListener {
                // 기존 선택 초기화
                for (j in 0 until group.childCount) {
                    val sibling = group.getChildAt(j)
                    sibling.isSelected = false
                    (sibling as? TextView)?.apply {
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                        setTextAppearance(R.style.body_regula)
                    }
                }
                // 현재 선택
                child.isSelected = true
                (child as? TextView)?.apply {
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.main1))
                    setTextAppearance(R.style.body_semibo)
                }

                // 어떤 그룹인지 식별
                when (group.id) {
                    R.id.gl_gender_group -> selectedGender = child
                    R.id.gl_age_group -> selectedAge = child
                    R.id.gl_job_group -> selectedJob = child
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

        // 선택 검증
        if (selectedGender == null || selectedAge == null || selectedJob == null) {
            Toast.makeText(requireContext(), "모든 항목을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 서버 전송
        val genderText = (selectedGender as TextView).text.toString()
        val ageText = (selectedAge as TextView).text.toString()
        val jobText = (selectedJob as TextView).text.toString()

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
        // 어두워지는 정도 설정 (0.0 ~ 1.0)
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

            if(code.isEmpty()) {
                errorTextView.visibility = View.VISIBLE
                errorTextView.text = "코드를 입력해주세요."
                return@setOnClickListener
            }

            if (code.length != 6) {
                errorTextView.visibility = View.VISIBLE
                errorTextView.text = "코드는 6자리여야 합니다."
                return@setOnClickListener
            }

            verifyReferralCodeApi(code,dialog,errorTextView)
        }

//        dialog.show()
    }

    // 초대 코드 검증
    private fun verifyReferralCodeApi(
        referralCode: String,
        dialog: Dialog,
        errorTextView: TextView
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val request = ReferralRequest(referralCode)
                val response = RetrofitClient.loginApiService.validateRecommendCode(request)
                //로그추가
                Timber.d("Referral code validation response: $response")

                if (!response.isSuccess) {
                    // 서버 응답 자체가 실패
                    errorTextView.visibility = View.VISIBLE
                    errorTextView.text = response.message // 예: "존재하지 않는 추천인 코드입니다."
                } else {
                        // 코드 유효 → 다음 화면 이동
                        dialog.dismiss()
                        navigateToTermsAgree(showRewardModal = true)
                }

            } catch (e: Exception) {
                // 네트워크 장애, 예외 발생 등
                // 만약 예외가 404 에러라면 "존재하지 않는 추천인 코드입니다." 메시지 출력
                if(e.message?.contains("404") == true) {
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
            else -> "UNDECIDED"
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
