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
import androidx.navigation.fragment.findNavController
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentSignupBinding // 자동 생성된 바인딩 클래스

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
        val gender = (selectedGender as TextView).text.toString()
        val age = (selectedAge as TextView).text.toString()
        val job = (selectedJob as TextView).text.toString()

        sendSignupDataToBackend(nickname, gender, age, job)

        // 회원가입 요청 성공 시
        showRecommendCodeDialog()
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

        // View Binding을 Dialog에도 적용하려면 별도의 Binding 클래스를 inflate 해주거나
        // findViewById 방식을 계속 사용할 수 있습니다.
        dialog.setContentView(R.layout.dialog_recommend_code)
        dialog.setCancelable(false)

        val etRecommendCode = dialog.findViewById<TextView>(R.id.et_recommend_code)
        val btnSkip = dialog.findViewById<View>(R.id.tv_skip)
        val btnConfirm = dialog.findViewById<View>(R.id.tv_recommend_close)
        val errorTextView = dialog.findViewById<TextView>(R.id.tv_recommend_code_error)

        // 건너뛰기
        btnSkip.setOnClickListener {
            dialog.dismiss()
            navigateToHomeScreen(showRewardModal = false)
        }

        // 확인
        btnConfirm.setOnClickListener {
            val code = etRecommendCode.text.toString()
            if (code.length == 6) {
                // TODO: 백엔드에서 코드 검증
                val isCodeValid = true // 임시
                if (isCodeValid) {
                    dialog.dismiss()
                    navigateToHomeScreen(showRewardModal = true)
                } else {
                    errorTextView.visibility = View.VISIBLE
                    errorTextView.text = "존재하지 않는 코드입니다."
                }
            } else {
                errorTextView.visibility = View.VISIBLE
            }
        }

        dialog.show()
    }

    private fun sendSignupDataToBackend(nickname: String, gender: String, age: String, job: String) {
        Toast.makeText(
            requireContext(),
            "회원가입 요청 전송: $nickname, $gender, $age, $job",
            Toast.LENGTH_SHORT
        ).show()
        // TODO: Retrofit 통신 등 실제 서버 전송
    }

    private fun navigateToHomeScreen(showRewardModal: Boolean) {
        val bundle = Bundle()
        bundle.putBoolean("showRewardModal", showRewardModal)
        findNavController().navigate(R.id.action_signupFragment_to_navigation_home, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
