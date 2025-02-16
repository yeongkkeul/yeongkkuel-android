package com.example.yeongkkuel.presentation.home.entry

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.bumptech.glide.Glide
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentExpenseViewBinding
import com.example.yeongkkuel.presentation.botsheet.BotSheetViewModel
import com.example.yeongkkuel.presentation.botsheet.BotSheetUiState
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.utils.DateUtils.parseDate
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Base64
import android.util.Log

class ExpenseViewFragment : Fragment() {

    private var _binding: FragmentExpenseViewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BotSheetViewModel by activityViewModels()
    private var selectedCategoryId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpenseViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) 뒤로가기, icMore 클릭 이벤트 등은 동일
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
            deleteExpense()
            binding.clMore.visibility = View.GONE
        }

        // 2) 번들에서 데이터 가져오기
        val expenseId = arguments?.getInt("expenseId") ?: 0
        val expenseName = arguments?.getString("expenseName") ?: ""
        val expensePrice = arguments?.getInt("expensePrice") ?: 0
        val categoryColor = arguments?.getInt("categoryColor") ?: R.color.black2
        val categoryName = arguments?.getString("categoryName") ?: "카테고리 없음"
        val expenseDateString = arguments?.getString("expenseDate") // yyyy-MM-dd 형식 가정
        val expenseDate = expenseDateString?.let { parseDate(it) }
        val imageUrl = arguments?.getString("imageUrl") ?: ""

//        // 무지출이면 `ic_more` 버튼 숨기기
//        if (expensePrice == 0) {
//            binding.icMore.visibility = View.GONE // 더보기 버튼 숨기기
//        } else {
//            binding.icMore.visibility = View.VISIBLE // 더보기 버튼 보이기
//        }

        // 3) Glide로 이미지 로드
        // ✅ Base64 또는 URL 기반 이미지 로드 처리
        if (imageUrl.isNotEmpty()) {
            if (imageUrl.startsWith("data:image")) {
                val base64Image = imageUrl.substringAfter(",")

                val decodedBitmap = decodeBase64ToBitmap(base64Image)
                if (decodedBitmap != null) {
                    binding.imgPhotoFrame.setImageBitmap(decodedBitmap)
                    binding.ivPhotoIcon.visibility = View.GONE
                } else {
                    showToast("이미지 로드 실패")
                }
            } else {
                Glide.with(binding.imgPhotoFrame.context)
                    .load(imageUrl)
                    .into(binding.imgPhotoFrame)
                binding.ivPhotoIcon.visibility = View.GONE  // 사진 있으면 아이콘 숨김
            }
        } else {
            // 사진이 없을 경우 기본 이미지 표시
            binding.imgPhotoFrame.setImageResource(R.drawable.bg_photo_input)
            binding.ivPhotoIcon.visibility = View.VISIBLE
        }

        // 4) 날짜 & 기타 UI 세팅
        binding.tvDateInput.text = formatDate(expenseDate)
        binding.etDetailInput.setText(expenseName)
        binding.etAmountInput.setText(formatPrice(expensePrice))
        binding.tvCategoryInput.setTextColor(ContextCompat.getColor(requireContext(), categoryColor))
        binding.tvCategoryInput.text = categoryName

        // 5) viewModel.uiState로부터 날짜 수시 갱신 등
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { uiState ->
                binding.tvDateInput.text = formatDate(uiState.date)
            }
        }

        // 6) 혹시 카테고리 탐색할 때 필요
        val matchedCategory = getCategoryForExpense(expenseId)
        selectedCategoryId = matchedCategory?.categoryId
    }


    private fun decodeBase64ToBitmap(base64Image: String?): Bitmap? {
        return try {
            if (base64Image.isNullOrEmpty()) return null

            val decodedBytes = Base64.decode(base64Image, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getCategoryForExpense(expenseId: Int): BotSheetUiState.Spending? {
        return viewModel.uiState.value.spendingList.find { spending ->
            spending.history.any { it.id == expenseId }
        }
    }

    private fun navigateToExpenseEdit() {
        val expenseId = arguments?.getInt("expenseId") ?: 0  // 현재 보고 있는 지출 ID 가져오기

        val selectedExpense = viewModel.spendingHistoryList.value.find { it.id == expenseId } // ✅ ID로 정확한 내역 찾기

        if (selectedExpense != null) {
            val bundle = Bundle().apply {
                putInt("expenseId", selectedExpense.id)
                putString("expenseName", selectedExpense.name)
                putInt("expensePrice", selectedExpense.price)
            }
            findNavController().navigate(R.id.action_ExpenseViewFragment_to_ExpenseEditFragment, bundle)
        } else {
            Toast.makeText(requireContext(), "지출 내역을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }


    // 지출 내역 삭제
    private fun deleteExpense() {
        val selectedExpense = viewModel.spendingHistoryList.value.lastOrNull() ?: return

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_expense_delete, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val tvExpenseTitle = dialogView.findViewById<TextView>(R.id.tv_expense_title)
        tvExpenseTitle.text = binding.etDetailInput.text.toString()
        val btnConfirm = dialogView.findViewById<TextView>(R.id.tv_delete_btn)
        val btnCancel = dialogView.findViewById<TextView>(R.id.tv_cancel_btn)

        btnConfirm.setOnClickListener {
            viewModel.deleteExpense(selectedExpense.id)

            viewModel.deleteResult.observe(viewLifecycleOwner) { isDeleted ->
                if (isDeleted) {
                    showToast("지출 내역이 삭제되었습니다.")
                    findNavController().popBackStack(R.id.navigation_home, false)
                } else {
                    showToast("삭제 실패. 다시 시도해주세요.")
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

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}