package com.example.yeongkkuel.presentation.home.entry

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.yeongkkuel.databinding.FragmentExpenseViewBinding
import com.example.yeongkkuel.presentation.botsheet.BotSheetViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.yeongkkuel.R // ✅ R import 추가
import com.example.yeongkkuel.presentation.botsheet.BotSheetUiState
import org.w3c.dom.Text
import java.util.Calendar
import java.util.Date

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
        binding.icMore.setOnClickListener {
            showEditDeleteMenu(it) // ✅ `meu_edit_delete` 표시 기능 추가
            expandClickArea(binding.icMore, 40) // ✅ 터치 영역 40dp 확장

        }



        // ✅ StateFlow를 collectLatest()로 감지
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.spendingHistoryList.collectLatest { historyList ->
                val selectedExpense = historyList.lastOrNull()

                if (selectedExpense != null) {
                    // ✅ 최신 spendingList 업데이트 대기 후 UI 업데이트
                    viewModel.uiState.collectLatest { uiState ->
                        binding.tvDateInput.text = formatDate(viewModel.uiState.value.date)

                        val category = getCategoryForExpense(selectedExpense.id)
                        selectedCategoryId = category?.categoryId //

                        binding.tvCategoryInput.text = category?.kind?.name ?: "기타"
                        binding.etDetailInput.setText(selectedExpense.name)
                        binding.etAmountInput.setText(selectedExpense.price.toString())

                        // ✅ 최신 데이터 반영 후 카테고리 색상 적용
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
    private fun showEditDeleteMenu(anchor: View) {
        val popupMenu = PopupMenu(requireContext(), anchor)
        popupMenu.menuInflater.inflate(R.menu.menu_edit_delete, popupMenu.menu)

        // ✅ 메뉴 아이템 클릭 리스너 설정
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_modify -> {
                    navigateToExpenseEdit()
                    true
                }
                R.id.action_delete -> {
                    deleteExpense()
                    true
                }
                else -> false
            }
        }

        popupMenu.show() // ✅ 팝업 메뉴 표시
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

    /**
     * ✅ 지출 내역 삭제 처리
     */
    private fun deleteExpense() {
        val selectedExpense = viewModel.spendingHistoryList.value.lastOrNull() ?: return

        // ✅ 다이얼로그 띄우기
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_expense_delete, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // ✅ 다이얼로그 내 버튼 설정
        val btnConfirm = dialogView.findViewById<TextView>(R.id.tv_delete_btn)
        val btnCancel = dialogView.findViewById<TextView>(R.id.tv_cancel_btn)

        btnConfirm.setOnClickListener {
            // ✅ 지출 내역 삭제 진행
            viewModel.removeExpense(selectedExpense.name)
            Toast.makeText(requireContext(), "지출 내역이 삭제되었습니다.", Toast.LENGTH_SHORT).show()

            dialog.dismiss() // 다이얼로그 닫기
            findNavController().popBackStack(R.id.navigation_home, false)

        }

        btnCancel.setOnClickListener {
            dialog.dismiss() // 다이얼로그 닫기 (취소)
        }

        // ✅ 다이얼로그 스타일 적용
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun getCategoryNameForExpense(expenseName: String): String {
        val spendingList = viewModel.uiState.value.spendingList

        // 🔹 `spendingList`에서 `History` 항목 중에서 해당하는 `name`이 포함된 `Spending`을 찾음
        val category = spendingList.find { spending ->
            spending.history.any { it.name == expenseName }
        }

        return category?.kind?.name ?: "기타" // ✅ 카테고리 이름 반환 (없으면 "기타")
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
