package com.example.yeongkkuel.presentation.home.entry

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.yeongkkuel.databinding.FragmentExpenseViewBinding
import com.example.yeongkkuel.presentation.botsheet.BotSheetViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentExpenseEditBinding
import com.example.yeongkkuel.presentation.botsheet.BotSheetUiState
import java.text.NumberFormat
import java.util.Date

class ExpenseEditFragment : Fragment() {

    private var _binding: FragmentExpenseEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BotSheetViewModel by activityViewModels()
    private var selectedCategoryId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpenseEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.tvExpenseTitleEdit.visibility = View.VISIBLE // 수정 제목 보이기
        binding.tvEntryComplete.visibility = View.VISIBLE
        binding.tvEntryComplete.setOnClickListener {
            saveExpense()
        }

        // ✅ 기존 지출 내역 불러오기
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.spendingHistoryList.collectLatest { historyList ->
                val selectedExpense = historyList.lastOrNull()

                if (selectedExpense != null) {
                    binding.tvDateInput.text = formatDate(viewModel.uiState.value.date)

                    val category = getCategoryForExpense(selectedExpense.id)
                    selectedCategoryId = category?.categoryId // ✅ 카테고리 ID 저장

                    binding.tvCategoryInput.text = category?.kind?.name ?: "기타"
                    binding.etDetailInput.setText(selectedExpense.name)

                    setupAmountInput()
                    binding.etAmountInput.setText(selectedExpense.price.toString())

                    // ✅ 카테고리 색상 적용
                    val updatedColor = getCategoryTextColor(category?.kind?.name ?: "기타")
                    binding.tvCategoryInput.setTextColor(updatedColor)

                    // ✅ 수정 가능하도록 `EditText` 활성화
                    enableEditing()
                }
            }
        }
    }
    private fun getCategoryForExpense(expenseId: Int): BotSheetUiState.Spending? {
        return viewModel.uiState.value.spendingList.find { spending ->
            spending.history.any { it.id == expenseId }
        }
    }


    private fun enableEditing() {
        binding.etDetailInput.isEnabled = true
        binding.etAmountInput.isEnabled = true
        binding.tvCategoryInput.isEnabled = false // 카테고리는 수정 불가능하도록 유지
    }

    private fun getCategoryNameForExpense(expenseId: Int): String {
        val spendingList = viewModel.uiState.value.spendingList

        // 🔹 expenseId 기반으로 해당 지출이 속한 카테고리를 찾음
        val category = spendingList.find { spending ->
            spending.history.any { it.id == expenseId }
        }

        return category?.kind?.name ?: "기타"
    }

    private fun formatDate(date: Date?): String {
        return if (date != null) {
            val sdf = SimpleDateFormat("yyyy년 M월 d일 E요일", Locale.KOREAN)
            sdf.format(date)
        } else {
            "날짜 없음"
        }
    }

    private fun getCategoryTextColor(categoryName: String): Int {
        val spendingList = viewModel.uiState.value.spendingList
        val category = spendingList.find { it.kind.name == categoryName }

        return category?.color?.id?.let { colorId ->
            ContextCompat.getColor(requireContext(), colorId) // ✅ 카테고리 색상 적용
        } ?: ContextCompat.getColor(requireContext(), R.color.black2) // 기본 색상 적용
    }

    private fun saveExpense() {
        val newDetail = binding.etDetailInput.text.toString().trim()
        val newAmount = binding.etAmountInput.text.toString().trim().replace(",", "").toIntOrNull() ?: 0

        if (newDetail.isEmpty() || newAmount <= 0) {
            Toast.makeText(requireContext(), "모든 항목을 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedExpense = viewModel.spendingHistoryList.value.lastOrNull()

        if (selectedExpense != null) {
            // ✅ 기존 카테고리 ID 유지
            val existingCategoryId = selectedCategoryId ?: return // 기존 카테고리 ID를 유지

            val updatedExpense = selectedExpense.copy(
                name = newDetail,
                price = newAmount
            )

            // ✅ suspend 함수는 Coroutine Scope 내에서 호출해야 함!
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.updateBotSheetHistory(updatedExpense)

                // ✅ 변경된 내역을 반영하기 위해 UIState를 갱신
                _binding?.let {
                    binding.etDetailInput.setText(newDetail)
                    binding.etAmountInput.setText(newAmount.toString())
                }
                Toast.makeText(requireContext(), "지출 내역이 수정되었습니다.", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_ExpenseEditFragment_to_HomeFragment)
            }
        }
    }
    private fun setupAmountInput() {
        binding.etAmountInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                binding.etAmountInput.removeTextChangedListener(this)

                val rawInput = s?.toString()?.replace(",", "") ?: ""
                val input = rawInput.toLongOrNull() ?: 0

                // 🔹 최대 8자리까지만 입력 가능하도록 제한
                val trimmedInput = if (rawInput.length > 8) rawInput.substring(0, 8) else rawInput
                val limitedValue = trimmedInput.toLongOrNull()?.coerceAtMost(99_999_999) ?: 0

                // 🔹 쉼표(,)를 자동으로 추가하여 1,000,000 형식으로 표시
                val formatted = NumberFormat.getInstance(Locale.KOREAN).format(limitedValue)

                if (formatted != s.toString()) {
                    binding.etAmountInput.setText(formatted)
                    binding.etAmountInput.setSelection(formatted.length)
                }

                binding.etAmountInput.addTextChangedListener(this)
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}