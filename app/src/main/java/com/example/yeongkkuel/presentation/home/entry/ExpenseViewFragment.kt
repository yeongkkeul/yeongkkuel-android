package com.example.yeongkkuel.presentation.home.entry

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentExpenseViewBinding
import com.example.yeongkkuel.presentation.botsheet.BotSheetUiState
import com.example.yeongkkuel.presentation.botsheet.BotSheetViewModel
import com.example.yeongkkuel.presentation.home.Expense
import com.example.yeongkkuel.presentation.home.entry.data.ExpenseViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpenseViewFragment : Fragment() {

    private var _binding: FragmentExpenseViewBinding? = null
    private val binding get() = _binding!!

    private val botSheetViewModel: BotSheetViewModel by activityViewModels()
    private val expenseViewModel: ExpenseViewModel by activityViewModels()

    private var selectedCategoryId: Int? = null
    private var expenseId: Int? = null


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
        binding.icMore.setOnClickListener {
            binding.clMore.visibility =
                if (binding.clMore.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        binding.clMore.findViewById<TextView>(R.id.tv_modify).setOnClickListener {
            navigateToExpenseEdit()
            binding.clMore.visibility = View.GONE
        }

        binding.clMore.findViewById<TextView>(R.id.tv_delete).setOnClickListener {
            val expenseId =
                arguments?.getInt("expenseId") ?: return@setOnClickListener // ✅ null이면 실행 안 함
            botSheetViewModel.deleteExpense(expenseId)
            deleteExpense()
            binding.clMore.visibility = View.GONE
        }


//        // 무지출이면 `ic_more` 버튼 숨기기
//        if (expensePrice == 0) {
//            binding.icMore.visibility = View.GONE // 더보기 버튼 숨기기
//        } else {
//            binding.icMore.visibility = View.VISIBLE // 더보기 버튼 보이기
//        }

        // 번들에서 데이터 가져오기
        expenseId = arguments?.getInt("expenseId")
        val expenseName = arguments?.getString("expenseName") ?: ""
        val expensePrice = arguments?.getInt("expensePrice") ?: 0
        val categoryColor = arguments?.getInt("categoryColor") ?: R.color.black2
        val categoryName = arguments?.getString("categoryName") ?: "카테고리 없음"
        val expenseDateString = arguments?.getString("expenseDate")
        val imageUrl = arguments?.getString("imageUrl") ?: ""

        // 이미지 로드
        if (imageUrl.isNotEmpty()) {
            Glide.with(binding.imgPhotoFrame.context)
                .load(imageUrl)
                .override(500, 500) // ✅ 크기 조정
                .centerCrop() // ✅ 꽉 차게 표시
                .into(binding.imgPhotoFrame) // ✅ 둥근 모서리는 XML에서 처리

            binding.ivPhotoIcon.visibility = View.GONE // ✅ 아이콘 숨김
        } else {
            binding.imgPhotoFrame.setImageResource(R.drawable.bg_photo_input)
            binding.ivPhotoIcon.visibility = View.VISIBLE
        }


        // "trash" 카테고리인지 확인 후 숨김 처리
        if (categoryName.lowercase() == "trash") {
            binding.tvExpenseCategory.visibility = View.GONE
            binding.tvCategoryInput.visibility = View.GONE
        } else {
            binding.tvExpenseCategory.visibility = View.VISIBLE
            binding.tvCategoryInput.visibility = View.VISIBLE
        }

        // 날짜 및 UI 설정
        binding.tvDateInput.text = expenseDateString ?: "날짜 없음"
        binding.etDetailInput.setText(expenseName)
        binding.etAmountInput.setText(formatPrice(expensePrice))
        binding.tvCategoryInput.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                categoryColor
            )
        )
        binding.tvCategoryInput.text = categoryName

        // UIState 데이터 갱신
        viewLifecycleOwner.lifecycleScope.launch {
            botSheetViewModel.uiState.collectLatest { uiState ->
                binding.tvDateInput.text = formatDate(uiState.date)
            }
        }

        // 카테고리 탐색할 때 필요
        val matchedCategory = expenseId?.let { getCategoryForExpense(it) }
        selectedCategoryId = matchedCategory?.categoryId
    }

    private fun getCategoryForExpense(expenseId: Int): BotSheetUiState.Spending? {
        return botSheetViewModel.uiState.value.spendingList.find { spending ->
            spending.history.any { it.id == expenseId }
        }
    }

    // 지출 수정 화면 이동
    private fun navigateToExpenseEdit() {
        if (expenseId == null) {
            showToast("지출 내역을 찾을 수 없습니다.")
            return
        }

        val selectedExpense =
            botSheetViewModel.spendingHistoryList.value.find { it.id == expenseId }
                ?: return

        val bundle = Bundle().apply {
            putInt("expenseId", selectedExpense.id)
            putString("expenseName", selectedExpense.name)
            putInt("expensePrice", selectedExpense.price)
            putString("expenseDate", binding.tvDateInput.text.toString())
            putString("imageUrl", arguments?.getString("imageUrl") ?: "")
        }
        findNavController().navigate(R.id.action_ExpenseViewFragment_to_ExpenseEditFragment, bundle)
    }


    // 지출 내역 삭제 (서버에도 반영)
    private fun deleteExpense() {
        if (expenseId == null) {
            showToast("삭제할 내역을 찾을 수 없습니다.")
            return
        }

        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_expense_delete, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val tvExpenseTitle = dialogView.findViewById<TextView>(R.id.tv_expense_title)
        tvExpenseTitle.text = binding.etDetailInput.text.toString()
        val btnConfirm = dialogView.findViewById<TextView>(R.id.tv_delete_btn)
        val btnCancel = dialogView.findViewById<TextView>(R.id.tv_cancel_btn)

        btnConfirm.setOnClickListener {
            expenseViewModel.deleteExpense(
                expenseId!!,
                isSuccess = {
                    botSheetViewModel.removeExpenseFromCategory(expenseId!!) // 바텀시트에서 삭제 반영
                    showToast("지출 내역이 삭제되었습니다.")
                    findNavController().popBackStack(R.id.navigation_home, false)
                },
                isFail = {
                    showToast("삭제 실패. 다시 시도해주세요.")
                })

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

    private fun formatPrice(price: Int): String {
        return NumberFormat.getInstance(Locale.KOREAN).format(price)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}