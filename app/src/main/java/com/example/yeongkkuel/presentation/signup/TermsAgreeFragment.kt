package com.example.yeongkkuel.presentation.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentTermsAgreeBinding

class TermsAgreeFragment : Fragment() {

    private var _binding: FragmentTermsAgreeBinding? = null
    private val binding get() = _binding!!

    // 각 체크항목의 체크 상태(기본 false)
    private var isCheckedAll = false
    private var isCheckedService = false
    private var isCheckedPrivacy = false
    private var isCheckedAge = false
    private var isCheckedThirdParty = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTermsAgreeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val showRewardModal = arguments?.getBoolean("showRewardModal") ?: false

        // 뒤로가기
        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // "전체 동의" 아이콘 클릭 -> 토글
        binding.ivCheckAll.setOnClickListener {
            isCheckedAll = !isCheckedAll
            setCheckAll(isCheckedAll)
            updateSignUpState()
        }

        // "서비스 이용 약관" 아이콘 클릭 -> 토글
        binding.ivCheckService.setOnClickListener {
            isCheckedService = !isCheckedService
            binding.ivCheckService.isSelected = isCheckedService
            updateCheckAllState()
            updateSignUpState()
        }

        // "개인정보 수집" 아이콘 클릭 -> 토글
        binding.ivCheckPrivacy.setOnClickListener {
            isCheckedPrivacy = !isCheckedPrivacy
            binding.ivCheckPrivacy.isSelected = isCheckedPrivacy
            updateCheckAllState()
            updateSignUpState()
        }

        // "만 14세 이상" 아이콘 클릭 -> 토글
        binding.ivCheckAge.setOnClickListener {
            isCheckedAge = !isCheckedAge
            binding.ivCheckAge.isSelected = isCheckedAge
            updateCheckAllState()
            updateSignUpState()
        }

        // "개인정보 제3자 제공 동의" (선택)
        binding.ivCheckThirdParty.setOnClickListener {
            isCheckedThirdParty = !isCheckedThirdParty
            binding.ivCheckThirdParty.isSelected = isCheckedThirdParty
            updateCheckAllState()
            updateSignUpState()
        }

        // 초기 버튼 상태 갱신
        updateSignUpState()

        binding.tvSignUp.setOnClickListener {
            navigateToHomeScreen(showRewardModal)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * "전체 동의" 체크/해제 시,
     * 모든 개별 항목에 적용 + 아이콘 반영
     */
    private fun setCheckAll(checked: Boolean) {
        // 전체동의 상태
        isCheckedAll = checked
        binding.ivCheckAll.isSelected = checked

        // 개별 항목
        isCheckedService = checked
        binding.ivCheckService.isSelected = checked

        isCheckedPrivacy = checked
        binding.ivCheckPrivacy.isSelected = checked

        isCheckedAge = checked
        binding.ivCheckAge.isSelected = checked

        isCheckedThirdParty = checked
        binding.ivCheckThirdParty.isSelected = checked
    }

    /**
     * 개별 항목 중 하나라도 false이면 전체동의 해제,
     * 모두 true이면 전체동의 체크
     */
    private fun updateCheckAllState() {
        val allChecked = isCheckedService && isCheckedPrivacy && isCheckedAge && isCheckedThirdParty
        if (allChecked != isCheckedAll) {
            isCheckedAll = allChecked
            binding.ivCheckAll.isSelected = allChecked
        }
    }

    /**
     * 필수 항목(서비스, 개인정보, 14세 이상)이 모두 true 여야 회원가입 버튼 활성화
     */
    private fun updateSignUpState() {
        val requiredChecked = isCheckedService && isCheckedPrivacy && isCheckedAge
        binding.tvSignUp.isEnabled = requiredChecked
    }

    /**
     * 회원가입 후 Home 화면으로 이동
     */
    private fun navigateToHomeScreen(showRewardModal: Boolean) {
        val bundle = Bundle()
        bundle.putBoolean("showRewardModal", showRewardModal)
        findNavController().navigate(R.id.action_termsAgreeFragment_to_navigation_home, bundle)
    }
}
