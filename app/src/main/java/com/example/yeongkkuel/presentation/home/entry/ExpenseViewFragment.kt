package com.example.yeongkkuel.presentation.home.entry

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentExpenseViewBinding
import com.example.yeongkkuel.presentation.botsheet.BotSheetViewModel
import com.example.yeongkkuel.presentation.botsheet.BotSheetUiState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpenseViewFragment : Fragment() {

    private var _binding: FragmentExpenseViewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BotSheetViewModel by activityViewModels()
    private var selectedCategoryId: Int? = null // ✅ 카테고리 ID 저장

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpenseViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // icMore 클릭 시 cl_more의 visibility 토글 (수정/삭제 메뉴 표시)
        binding.icMore.setOnClickListener {
            binding.clMore.visibility =
                if (binding.clMore.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        // cl_more 내 수정 버튼 클릭 이벤트
        binding.clMore.findViewById<TextView>(R.id.tv_modify).setOnClickListener {
            navigateToExpenseEdit()
            binding.clMore.visibility = View.GONE
        }

        // cl_more 내 삭제 버튼 클릭 이벤트
        binding.clMore.findViewById<TextView>(R.id.tv_delete).setOnClickListener {
            deleteExpense()
            binding.clMore.visibility = View.GONE
        }

        // ✅ StateFlow를 collectLatest()로 감지해서 최신 데이터를 UI에 반영
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.spendingHistoryList.collectLatest { historyList ->
                val selectedExpense = historyList.lastOrNull()

                if (selectedExpense != null) {
                    // 최신 spendingList 업데이트 후 UI 업데이트
                    viewModel.uiState.collectLatest { uiState ->
                        binding.tvDateInput.text = formatDate(uiState.date)

                        val category = getCategoryForExpense(selectedExpense.id)
                        selectedCategoryId = category?.categoryId

                        binding.tvCategoryInput.text = category?.kind?.name ?: "기타"
                        binding.etDetailInput.setText(selectedExpense.name)
                        binding.etAmountInput.setText(selectedExpense.price.toString())

                        // 최신 데이터 반영 후 카테고리 색상 적용
                        val updatedColor = getCategoryTextColor(category?.kind?.name ?: "기타")
                        binding.tvCategoryInput.setTextColor(updatedColor)
                    }
                }
            }
        }
    }

    private fun getCategoryForExpense(expenseId: Int): BotSheetUiState.Spending? {
        return viewModel.uiState.value.spendingList.find { spending ->
            spending.history.any { it.id == expenseId }
        }
    }

    private fun navigateToExpenseEdit() {
        val selectedExpense = viewModel.spendingHistoryList.value.lastOrNull()
        if (selectedExpense != null) {
            val bundle = Bundle().apply {
                putInt("expenseId", selectedExpense.id)
                putString("expenseName", selectedExpense.name)
                putInt("expensePrice", selectedExpense.price)
            }
            findNavController().navigate(R.id.action_ExpenseViewFragment_to_ExpenseEditFragment, bundle)
        }
    }

    // 지출 내역 삭제
    private fun deleteExpense() {
        val selectedExpense = viewModel.spendingHistoryList.value.lastOrNull() ?: return

        // 삭제 확인 다이얼로그 띄우기
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_expense_delete, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val tvExpenseTitle = dialogView.findViewById<TextView>(R.id.tv_expense_title)
        tvExpenseTitle.text = binding.etDetailInput.text.toString() // ✅ 수정된 부분
        val btnConfirm = dialogView.findViewById<TextView>(R.id.tv_delete_btn)
        val btnCancel = dialogView.findViewById<TextView>(R.id.tv_cancel_btn)

        btnConfirm.setOnClickListener {
            viewModel.deleteExpense(selectedExpense.id) // ✅ 서버에 삭제 요청

            viewModel.deleteResult.observe(viewLifecycleOwner) { isDeleted ->
                if (isDeleted) {
                    Toast.makeText(requireContext(), "지출 내역이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack(R.id.navigation_home, false)
                } else {
                    Toast.makeText(requireContext(), "삭제 실패. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
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
            ContextCompat.getColor(requireContext(), colorId)
        } ?: ContextCompat.getColor(requireContext(), R.color.black2)
    }

    // 필요시 터치 영역 확장 (옵션)
    private fun expandClickArea(view: View, extraPadding: Int) {
        val parent = view.parent as View
        parent.post {
            val rect = Rect()
            view.getHitRect(rect)
            rect.top -= extraPadding
            rect.bottom += extraPadding
            rect.left -= extraPadding
            rect.right += extraPadding
            parent.touchDelegate = TouchDelegate(rect, view)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
