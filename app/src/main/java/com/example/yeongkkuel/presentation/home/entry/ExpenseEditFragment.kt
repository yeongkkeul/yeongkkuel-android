package com.example.yeongkkuel.presentation.home.entry

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.yeongkkuel.presentation.botsheet.BotSheetViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentExpenseEditBinding
import com.example.yeongkkuel.presentation.botsheet.BotSheetUiState
import com.example.yeongkkuel.presentation.home.entry.data.ExpenseUpdateRequest
import java.text.NumberFormat
import java.util.Calendar
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

        setupDetailInput() // 지출 내용 글자 수 카운트

        binding.tvDateInput.setOnClickListener { showDatePickerDialog() } // 달력 설정

        // 기존 지출 내역 불러오기
        val selectedExpenseId = arguments?.getInt("expenseId") ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.spendingHistoryList.collectLatest { historyList ->
                val selectedExpense = historyList.find { it.id == selectedExpenseId } // 🔥 선택한 지출 내역 찾기

                if (selectedExpense != null) {
                    binding.tvDateInput.text = formatDate(viewModel.uiState.value.date)

                    val category = getCategoryForExpense(selectedExpense.id)
                    selectedCategoryId = category?.categoryId

                    binding.tvCategoryInput.text = category?.kind?.name ?: "기타"
                    binding.etDetailInput.setText(selectedExpense.name)

                    setupAmountInput()
                    binding.etAmountInput.setText(selectedExpense.price.toString())

                    val updatedColor = getCategoryTextColor(category?.kind?.name ?: "기타")
                    binding.tvCategoryInput.setTextColor(updatedColor)

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

//    private fun getCategoryNameForExpense(expenseId: Int): String {
//        val spendingList = viewModel.uiState.value.spendingList
//
//        // 🔹 expenseId 기반으로 해당 지출이 속한 카테고리를 찾음
//        val category = spendingList.find { spending ->
//            spending.history.any { it.id == expenseId }
//        }
//
//        return category?.kind?.name ?: "기타"
//    }

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

        // arguments에서 expenseId 가져오기
        val selectedExpenseId = arguments?.getInt("expenseId") ?: return

        // 클릭한 내역을 찾아서 업데이트
        val selectedExpense = viewModel.spendingHistoryList.value.find { it.id == selectedExpenseId } ?: return
        val category = getCategoryForExpense(selectedExpense.id)

        val expenseImage =
            if (category?.history?.find { it.id == selectedExpense.id }?.imgExist == true) {
                "" // 서버에서 기존 이미지를 유지하도록 설정
            } else {
                null // 이미지가 없을 경우 null로 설정
            }

        // ✅ 사용자가 선택한 날짜가 있으면 그걸 사용하고, 없으면 기존 날짜 사용
        val updatedDate = selectedDate?.let { formatDateToApiFormat(it) }
            ?: formatDateToApiFormat(viewModel.uiState.value.date)

        Log.d("ExpenseEditFragment", "✅ 수정된 날짜: $updatedDate")

        val updatedExpense = ExpenseUpdateRequest(
            day = updatedDate, // 변경된 날짜 반영
            categoryId = selectedCategoryId ?: return,
            content = newDetail,
            amount = newAmount,
            expenseImg = expenseImage
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val updateResponse = viewModel.updateExpense(selectedExpense.id, updatedExpense)

                if (updateResponse?.isSuccess == true) {
                    // 기존 카테고리에서 삭제 후 - 새로운 날짜로 이동
                    viewModel.removeExpenseFromCategory(selectedExpense.id)
                    viewModel.moveExpenseToNewDate(selectedExpense, updatedDate)

                    Toast.makeText(requireContext(), "지출 내역이 수정되었습니다.", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_ExpenseEditFragment_to_HomeFragment)
                } else {
                    Toast.makeText(requireContext(), "수정 실패: ${updateResponse?.message ?: "알 수 없는 오류"}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "네트워크 오류 발생. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // 지출 내용 글자 수 제한 로직 추가
    private fun setupDetailInput() {
        val etDetailInput = binding.etDetailInput
        val tvCharacterCount = binding.tvCharacterCount

        etDetailInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                tvCharacterCount.text = "$length/24" // 글자 수 표시

                if (length > 24) {
                    etDetailInput.error = "최대 24자까지 입력 가능합니다."
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
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

    private var selectedDate: Date? = null // ✅ 사용자가 선택한 날짜 저장

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val pickedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }.time

                selectedDate = pickedDate // ✅ 선택한 날짜 저장
                binding.tvDateInput.text = formatDate(pickedDate) // ✅ UI 반영
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }


    private fun parseDateFromDisplay(displayDate: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy년 M월 d일 E요일", Locale.KOREAN)
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN) // ✅ 서버 요구 형식

            val date = inputFormat.parse(displayDate)
            date?.let { outputFormat.format(it) } ?: outputFormat.format(Date()) // ✅ 변환 실패 시 현재 날짜 반환
        } catch (e: Exception) {
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
            outputFormat.format(Date()) // ✅ 예외 발생 시 기본값 반환
        }
    }


    private fun formatDateToApiFormat(date: Date?): String {
        return if (date != null) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
            sdf.format(date)
        } else {
            ""
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}