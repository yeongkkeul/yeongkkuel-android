package com.example.yeongkkuel.presentation.chat

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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FilterAgeDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentFilterAgeDialogBinding? = null
    private val binding: FragmentFilterAgeDialogBinding
        get() = requireNotNull(_binding){"FragmentFilterAgeDialogBinding -> null"}

    private var selectedAgeOption: String? = null

    private val viewModel: ChatSearchViewModel by activityViewModels()

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

        options.forEach { option ->
            option.setOnClickListener {
                selectedAgeOption = option.text.toString()
                viewModel.selectedAgeOption.value = selectedAgeOption

                updateOptionSelection(selectedOption = option, options = options)
            }
        }
    }

    private fun updateOptionSelection(selectedOption: TextView, options: List<TextView>) {
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.main1)
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.black)
        options.forEach { option ->
            option.setTextColor(if (option == selectedOption) selectedColor else defaultColor)
            option.setTypeface(null, if (option == selectedOption) Typeface.BOLD else Typeface.NORMAL)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
