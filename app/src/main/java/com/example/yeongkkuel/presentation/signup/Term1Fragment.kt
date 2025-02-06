package com.example.yeongkkuel.presentation.signup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentTerm1Binding
import com.example.yeongkkuel.databinding.FragmentTermsAgreeBinding


class Term1Fragment : Fragment() {

    private var _binding: FragmentTerm1Binding? = null
    private val binding get() = _binding!!

    // TermsAgreeViewModel 을 Activity 범위에서 공유
    private val viewModel: TermsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTerm1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 초기 체크 상태 UI 반영
        val isChecked = viewModel.isCheckedService.value ?: false
        binding.ivCheckService.isSelected = isChecked

        // (1) 뒤로가기 버튼 클릭 시 뒤로감
        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // (2) 체크 해제된 상태에서 체크 시 자동으로 뒤로가기,
        // (3) 체크된 상태에서 체크 해제하면 뒤로가지 않음
        binding.ivCheckService.setOnClickListener {
            val wasChecked = viewModel.isCheckedService.value == true
            val newChecked = !wasChecked

            // 뷰모델에도 상태 반영
            viewModel.isCheckedService.value = newChecked
            // 현재 프래그먼트의 UI도 업데이트
            binding.ivCheckService.isSelected = newChecked

            // 체크 해제->체크 시 자동으로 뒤로가기
            if (!wasChecked && newChecked) {
                parentFragmentManager.popBackStack()
            }
            // 체크->해제 시에는 뒤로가지 않음
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}