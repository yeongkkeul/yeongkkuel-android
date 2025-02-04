package com.example.yeongkkuel.presentation.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentTermsAgreeBinding

class TermsAgreeFragment : Fragment() {

    private var _binding: FragmentTermsAgreeBinding? = null
    private val binding get() = _binding!!

    // 전체 동의 상태는 Fragment 내에서만 간단히 관리
    private var isCheckedAll = false

    // ViewModel (Activity 범위로 공유)
    private val viewModel: TermsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTermsAgreeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val showRewardModal = arguments?.getBoolean("showRewardModal") ?: false

        initListener()
        observeViewModel()

        // 초기 버튼 상태 갱신
        updateSignUpState()

        // 회원가입 버튼 클릭
        binding.tvSignUp.setOnClickListener {
            navigateToHomeScreen(showRewardModal)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * 클릭 리스너 등록
     */
    private fun initListener() {
        // **Bundle로 Boolean 값을 넘기지 않고 단순 이동만 처리**
        binding.tvCheckService.setOnClickListener {
            findNavController().navigate(R.id.action_termsAgreeFragment_to_navigation_term_1)
        }
        binding.tvCheckPrivacy.setOnClickListener {
            findNavController().navigate(R.id.action_termsAgreeFragment_to_navigation_term_2)
        }
        binding.tvCheckAge.setOnClickListener {
            findNavController().navigate(R.id.action_termsAgreeFragment_to_navigation_term_3)
        }
        binding.tvCheckThirdParty.setOnClickListener {
            findNavController().navigate(R.id.action_termsAgreeFragment_to_navigation_term_4)
        }

        // 뒤로가기
        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // \"전체 동의\" 아이콘 클릭 -> 토글
        binding.ivCheckAll.setOnClickListener {
            isCheckedAll = !isCheckedAll
            setCheckAll(isCheckedAll)
            updateSignUpState()
        }

        // 각 항목 클릭 시 ViewModel 값만 변경
        binding.ivCheckService.setOnClickListener {
            viewModel.isCheckedService.value = !(viewModel.isCheckedService.value ?: false)
        }
        binding.ivCheckPrivacy.setOnClickListener {
            viewModel.isCheckedPrivacy.value = !(viewModel.isCheckedPrivacy.value ?: false)
        }
        binding.ivCheckAge.setOnClickListener {
            viewModel.isCheckedAge.value = !(viewModel.isCheckedAge.value ?: false)
        }
        binding.ivCheckThirdParty.setOnClickListener {
            viewModel.isCheckedThirdParty.value = !(viewModel.isCheckedThirdParty.value ?: false)
        }
    }

    /**
     * ViewModel을 관찰하여 UI 갱신
     */
    private fun observeViewModel() {
        viewModel.isCheckedService.observe(viewLifecycleOwner) {
            binding.ivCheckService.isSelected = it
            updateCheckAllState()
            updateSignUpState()
        }

        viewModel.isCheckedPrivacy.observe(viewLifecycleOwner) {
            binding.ivCheckPrivacy.isSelected = it
            updateCheckAllState()
            updateSignUpState()
        }

        viewModel.isCheckedAge.observe(viewLifecycleOwner) {
            binding.ivCheckAge.isSelected = it
            updateCheckAllState()
            updateSignUpState()
        }

        viewModel.isCheckedThirdParty.observe(viewLifecycleOwner) {
            binding.ivCheckThirdParty.isSelected = it
            updateCheckAllState()
            updateSignUpState()
        }
    }

    /**
     * \"전체 동의\" 체크/해제 시, ViewModel에 있는 개별 항목에 적용
     */
    private fun setCheckAll(checked: Boolean) {
        binding.ivCheckAll.isSelected = checked
        viewModel.isCheckedService.value = checked
        viewModel.isCheckedPrivacy.value = checked
        viewModel.isCheckedAge.value = checked
        viewModel.isCheckedThirdParty.value = checked
    }

    /**
     * ViewModel 상태를 보고 전체 동의 여부 갱신
     */
    private fun updateCheckAllState() {
        val allChecked = (
                (viewModel.isCheckedService.value == true) &&
                        (viewModel.isCheckedPrivacy.value == true) &&
                        (viewModel.isCheckedAge.value == true) &&
                        (viewModel.isCheckedThirdParty.value == true)
                )
        if (allChecked != isCheckedAll) {
            isCheckedAll = allChecked
            binding.ivCheckAll.isSelected = allChecked
        }
    }

    /**
     * 필수 항목(서비스, 개인정보, 14세 이상)이 모두 true 여야 회원가입 버튼 활성화
     */
    private fun updateSignUpState() {
        val requiredChecked = (
                (viewModel.isCheckedService.value == true) &&
                        (viewModel.isCheckedPrivacy.value == true) &&
                        (viewModel.isCheckedAge.value == true)
                )
        binding.tvSignUp.isEnabled = requiredChecked
    }

    /**
     * 회원가입 후 Home 화면으로 이동
     */
    private fun navigateToHomeScreen(showRewardModal: Boolean) {
        val bundle = Bundle().apply {
            putBoolean("showRewardModal", showRewardModal)
        }
        findNavController().navigate(R.id.action_termsAgreeFragment_to_navigation_home, bundle)
    }
}
