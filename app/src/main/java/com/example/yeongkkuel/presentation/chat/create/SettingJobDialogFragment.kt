package com.example.yeongkkuel.presentation.chat.create

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentFilterJobDialogBinding
import com.example.yeongkkuel.presentation.chat.data.Job
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SettingJobDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentFilterJobDialogBinding? = null
    private val binding: FragmentFilterJobDialogBinding
        get() = requireNotNull(_binding) { "FragmentFilterJobDialogBinding -> null" }

    private val viewModel: ChatRoomCreateViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentFilterJobDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val options = listOf(
            binding.btnStatusStudent,
            binding.btnStatusHousewife,
            binding.btnStatusWorker,
            binding.btnStatusSelfemployed
        )

        // 다이얼로그가 열릴 때 이미 선택된 옵션이 있다면 해당 버튼 활성화 처리
        viewModel.selectedJobOption.value?.let { selectedJob ->
            val selectedButton = options.find { it.text.toString() == selectedJob }
            updateOptionSelection(selectedButton, options)
        } ?: updateOptionSelection(null, options)

        options.forEach { option ->
            option.setOnClickListener {
                val selectedJob = option.text.toString()
                if (viewModel.selectedJobOption.value == selectedJob) {
                    // 이미 선택된 상태라면 선택 해제
                    viewModel.selectedJobOption.value = null
                    updateOptionSelection(null, options)
                } else {
                    viewModel.selectedJobOption.value = selectedJob
                    updateOptionSelection(option, options)
                }
            }
        }
    }

    private fun updateOptionSelection(selectedOption: TextView?, options: List<TextView>) {
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.main1)
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.black)
        options.forEach { option ->
            if (option == selectedOption) {
                option.setTextColor(selectedColor)
                option.setTypeface(null, Typeface.BOLD)
            } else {
                option.setTextColor(defaultColor)
                option.setTypeface(null, Typeface.NORMAL)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
