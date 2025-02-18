package com.example.yeongkkuel.presentation.chat.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.yeongkkuel.databinding.FragmentFilterAmountPeopleDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SettingAmountPeopleDialogFragment(
    private val onExpenseSelected: (Int) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: FragmentFilterAmountPeopleDialogBinding? = null
    private val binding: FragmentFilterAmountPeopleDialogBinding
        get() = requireNotNull(_binding) { "FragmentFilterAmountPeopleDialogBinding -> null" }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterAmountPeopleDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etAmountPeople.requestFocus()
        binding.etAmountPeople.post {
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etAmountPeople, InputMethodManager.SHOW_IMPLICIT)
        }

        binding.etAmountPeople.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val peopleStr = binding.etAmountPeople.text.toString().trim()
                if (peopleStr.isNotEmpty()) {
                    val people = peopleStr.toIntOrNull()
                    if (people != null) {
                        if (people in 5..100) {
                            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(binding.etAmountPeople.windowToken, 0)

                            onExpenseSelected(people)
                            dismiss()
                        } else {
                            // 범위를 벗어난 경우 경고 텍스트를 표시함
                            if (people < 5) {
                                binding.tvGuide.visibility = View.VISIBLE
                                binding.tvWarning.visibility = View.GONE
                            } else {
                                binding.tvGuide.visibility = View.GONE
                                binding.tvWarning.visibility = View.VISIBLE
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "숫자를 입력해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
                true
            } else {
                false
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
