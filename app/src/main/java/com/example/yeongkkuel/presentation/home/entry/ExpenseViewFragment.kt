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
import java.text.NumberFormat

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
        expandClickArea(binding.icMore, 20)

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

        // Bundle에서 데이터 가져와서 UI에 표시
        val expenseId = arguments?.getInt("expenseId") ?: 0
        val expenseName = arguments?.getString("expenseName") ?: ""
        val expensePrice = arguments?.getInt("expensePrice") ?: 0
        val categoryColor = arguments?.getInt("categoryColor") ?: R.color.black2
        val categoryName = arguments?.getString("categoryName") ?: "카테고리 없음"

        // 이제 이 값들을 UI에 세팅
        binding.etDetailInput.setText(expenseName)
        binding.etAmountInput.setText(formatPrice(expensePrice)) // 쉼표 포함 숫자 표시

        binding.tvCategoryInput.setTextColor(ContextCompat.getColor(requireContext(), categoryColor))
        binding.tvCategoryInput.text = categoryName
        binding.tvCategoryInput.setTextColor(ContextCompat.getColor(requireContext(), categoryColor))

        // 날짜 등은 viewModel에서 가져올 수 있음
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { uiState ->
                binding.tvDateInput.text = formatDate(uiState.date)
            }
        }

        // 필요하면 getCategoryForExpense(expenseId)로 카테고리 찾기
        val matchedCategory = getCategoryForExpense(expenseId)
        selectedCategoryId = matchedCategory?.categoryId
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
        tvExpenseTitle.text = binding.etDetailInput.text.toString()
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

    private fun getCategoryTextColor(categoryName: String): Int {
        val spendingList = viewModel.uiState.value.spendingList
        val category = spendingList.find { it.kind.name == categoryName }
        return category?.color?.id?.let { colorId ->
            ContextCompat.getColor(requireContext(), colorId)
        } ?: ContextCompat.getColor(requireContext(), R.color.black2)
    }

    private fun formatPrice(price: Int): String {
        return NumberFormat.getInstance(Locale.KOREAN).format(price)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}