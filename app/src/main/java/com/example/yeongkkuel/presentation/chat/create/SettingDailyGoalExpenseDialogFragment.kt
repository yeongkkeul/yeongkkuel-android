package com.example.yeongkkuel.presentation.chat.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.yeongkkuel.databinding.FragmentFilterDailyGoalExpenseDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SettingDailyGoalExpenseDialogFragment(
    private val onExpenseSelected: (Int) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: FragmentFilterDailyGoalExpenseDialogBinding? = null
    private val binding: FragmentFilterDailyGoalExpenseDialogBinding
        get() = requireNotNull(_binding) { "FragmentFilterDailyGoalExpenseDialogBinding -> null" }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterDailyGoalExpenseDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etDailyGoalExpense.requestFocus()
        binding.etDailyGoalExpense.post {
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etDailyGoalExpense, InputMethodManager.SHOW_IMPLICIT)
        }

        binding.etDailyGoalExpense.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val expenseStr = binding.etDailyGoalExpense.text.toString().trim()
                if (expenseStr.isNotEmpty()) {
                    val expense = expenseStr.toIntOrNull()
                    if (expense != null) {

                        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(binding.etDailyGoalExpense.windowToken, 0)

                        onExpenseSelected(expense)
                        dismiss()
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
