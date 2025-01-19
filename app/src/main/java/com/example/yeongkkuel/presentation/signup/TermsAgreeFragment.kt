package com.example.yeongkkuel.presentation.signup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.yeongkkuel.R

class TermsAgreeFragment : Fragment() {

    // 뒤로가기
    private lateinit var ivBack: ImageView

    // "전체 동의"
    private lateinit var ivCheckAll: ImageView

    // "서비스 이용 약관(필수)"
    private lateinit var ivCheckService: ImageView

    // "개인정보 수집 및 이용(필수)"
    private lateinit var ivCheckPrivacy: ImageView

    // "만 14세 이상 확인(필수)"
    private lateinit var ivCheckAge: ImageView

    // "개인정보 제3자 제공 동의(선택)"
    private lateinit var ivCheckThirdParty: ImageView

    // 회원가입 버튼 (TextView)
    private lateinit var tvSignUp: TextView

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
        return inflater.inflate(R.layout.fragment_terms_agree, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뷰 참조
        ivBack = view.findViewById(R.id.ivBack)

        ivCheckAll = view.findViewById(R.id.ivCheckAll)
        ivCheckService = view.findViewById(R.id.ivCheckService)
        ivCheckPrivacy = view.findViewById(R.id.ivCheckPrivacy)
        ivCheckAge = view.findViewById(R.id.ivCheckAge)
        ivCheckThirdParty = view.findViewById(R.id.ivCheckThirdParty)

        tvSignUp = view.findViewById(R.id.tvSignUp)

        val showRewardModal = arguments?.getBoolean("showRewardModal") ?: false

        // 뒤로가기
        ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // "전체 동의" 아이콘 클릭 -> 토글
        ivCheckAll.setOnClickListener {
            isCheckedAll = !isCheckedAll
            setCheckAll(isCheckedAll)
            updateSignUpState()
        }

        // "서비스 이용 약관" 아이콘 클릭 -> 토글
        ivCheckService.setOnClickListener {
            isCheckedService = !isCheckedService
            ivCheckService.isSelected = isCheckedService
            updateCheckAllState()
            updateSignUpState()
        }

        // "개인정보 수집" 아이콘 클릭 -> 토글
        ivCheckPrivacy.setOnClickListener {
            isCheckedPrivacy = !isCheckedPrivacy
            ivCheckPrivacy.isSelected = isCheckedPrivacy
            updateCheckAllState()
            updateSignUpState()
        }

        // "만 14세 이상" 아이콘 클릭 -> 토글
        ivCheckAge.setOnClickListener {
            isCheckedAge = !isCheckedAge
            ivCheckAge.isSelected = isCheckedAge
            updateCheckAllState()
            updateSignUpState()
        }

        // "개인정보 제3자 제공 동의" (선택)
        ivCheckThirdParty.setOnClickListener {
            isCheckedThirdParty = !isCheckedThirdParty
            ivCheckThirdParty.isSelected = isCheckedThirdParty
            updateCheckAllState()
            updateSignUpState()
        }

        // 초기 버튼 상태 갱신
        updateSignUpState()

        tvSignUp.setOnClickListener {
            navigateToHomeScreen(showRewardModal)
        }
    }

    /**
     * "전체 동의" 체크/해제 시,
     * 모든 개별 항목에 적용 + 아이콘 반영
     */
    private fun setCheckAll(checked: Boolean) {
        // 전체동의 상태
        isCheckedAll = checked
        ivCheckAll.isSelected = checked

        // 개별 항목
        isCheckedService = checked
        ivCheckService.isSelected = checked

        isCheckedPrivacy = checked
        ivCheckPrivacy.isSelected = checked

        isCheckedAge = checked
        ivCheckAge.isSelected = checked

        isCheckedThirdParty = checked
        ivCheckThirdParty.isSelected = checked
    }

    /**
     * 개별 항목 중 하나라도 false이면 전체동의 해제,
     * 모두 true이면 전체동의 체크
     */
    private fun updateCheckAllState() {
        val allChecked = isCheckedService && isCheckedPrivacy && isCheckedAge && isCheckedThirdParty
        if (allChecked != isCheckedAll) {
            isCheckedAll = allChecked
            ivCheckAll.isSelected = allChecked
        }
    }

    /**
     * 필수 항목(서비스, 개인정보, 14세 이상)이 모두 true 여야 회원가입 버튼 활성화
     */
    private fun updateSignUpState() {
        val requiredChecked = isCheckedService && isCheckedPrivacy && isCheckedAge
        tvSignUp.isEnabled = requiredChecked
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
