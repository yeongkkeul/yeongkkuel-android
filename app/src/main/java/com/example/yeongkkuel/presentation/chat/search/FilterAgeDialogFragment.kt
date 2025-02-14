package com.example.yeongkkuel.presentation.chat.search

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentFilterAgeDialogBinding
import com.example.yeongkkuel.presentation.chat.data.Age
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FilterAgeDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentFilterAgeDialogBinding? = null
    private val binding: FragmentFilterAgeDialogBinding
        get() = requireNotNull(_binding) { "FragmentFilterAgeDialogBinding -> null" }

    private val viewModel: ChatSearchViewModel by activityViewModels()

    private fun getAgeFromButton(option: TextView): Age {
        return when (option.text.toString()) {
            "미선택" -> Age.UNDECIDED
            "14~19세" -> Age.TEENAGER
            "20대" -> Age.TWENTIES
            "30대" -> Age.THIRTIES
            "40대" -> Age.FORTIES
            "50대" -> Age.FIFTIES
            "60세 이상" -> Age.SIXTIES_AND_ABOVE
            else -> Age.UNDECIDED
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterAgeDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val options = listOf(
            binding.btnAge1419,
            binding.btnAge30,
            binding.btnAge50,
            binding.btnAge20,
            binding.btnAge40,
            binding.btnAge60
        )

        // 다이얼로그가 열릴 때, 이미 선택되어 있는 옵션에 맞게 버튼 활성화 처리
        viewModel.selectedAgeOption.value?.let { selectedAge ->
            val selectedButton = options.find { getAgeFromButton(it) == selectedAge }
            updateOptionSelection(selectedButton, options)
        } ?: updateOptionSelection(null, options)

        options.forEach { option ->
            option.setOnClickListener {
                val selectedAge = getAgeFromButton(option)
                // 이미 선택된 상태라면 해제 (toggle off)
                if (viewModel.selectedAgeOption.value == selectedAge) {
                    viewModel.selectedAgeOption.value = null
                    updateOptionSelection(null, options)
                } else {
                    viewModel.selectedAgeOption.value = selectedAge
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
