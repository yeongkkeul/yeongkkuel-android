package com.example.yeongkkuel.presentation.home.entry

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentExpenseEntryBinding
import com.example.yeongkkuel.databinding.ItemMenuPopupBinding
import com.example.yeongkkuel.presentation.botsheet.BotSheetUiState
import com.example.yeongkkuel.presentation.botsheet.BotSheetViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpenseViewFragment : Fragment() {

    private var _binding: FragmentExpenseEntryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BotSheetViewModel by activityViewModels()  // ✅ 중복 제거

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpenseEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ 최신 내역을 감시하여 UI 업데이트
        observeLatestHistory()

        // ✅ 수정된 데이터가 반영되도록 설정
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Bundle>("editedExpense")
            ?.observe(viewLifecycleOwner) { bundle ->
                bundle?.let {
                    val updatedExpense = BotSheetUiState.Spending.History(
                        date = it.getString("expenseDate", getCurrentDate()),
                        categoryName = it.getString("categoryName", ""),
                        categoryColor = it.getString("categoryColor", ""),
                        name = it.getString("expenseContent", ""),
                        content = it.getString("expenseContent", ""),
                        price = it.getInt("expensePrice", 0),
                        photoUrl = it.getString("expensePhoto", "")
                    )
                    Log.d("ExpenseViewFragment", "Received editedExpense: $updatedExpense")
                    updateUiWithHistory(updatedExpense)
                }
            }

        setupUi()
        Log.d("ExpenseViewFragment", "📌 arguments: $arguments") // ✅ 전체 arguments 확인

        val expenseDate = arguments?.getString("expenseDate", getCurrentDate())
        val categoryName = arguments?.getString("categoryName", "기본 카테고리")
        val categoryColorStr = arguments?.getString("categoryColor") // ✅ HEX 값으로 받을 경우
        val categoryColorResId: Int = arguments?.getInt("categoryColor") ?: -1

        Log.d("ExpenseViewFragment", "📌 expenseDate: $expenseDate, categoryName: $categoryName, categoryColorStr: $categoryColorStr, categoryColorResId: $categoryColorResId")

        setupCategory(categoryName, categoryColorStr, categoryColorResId)
        binding.icMore.setOnClickListener { view ->
            showCustomMenu(view)
        }
    }
    private fun setupCategory(categoryName: String?, categoryColorStr: String?, categoryColorResId: Int) {
        binding.tvCategoryInput.text = categoryName ?: "기본 카테고리"

        try {
            when {
                !categoryColorStr.isNullOrEmpty() && categoryColorStr.startsWith("#") -> {
                    // ✅ HEX 코드 처리
                    binding.tvCategoryInput.setTextColor(Color.parseColor(categoryColorStr))
                    Log.d("ExpenseViewFragment", "✅ HEX 색상 적용됨: $categoryColorStr")
                }
                categoryColorResId != -1 -> {
                    // ✅ 리소스 ID 처리
                    val resolvedColor = ContextCompat.getColor(requireContext(), categoryColorResId)
                    binding.tvCategoryInput.setTextColor(resolvedColor)
                    Log.d("ExpenseViewFragment", "✅ 리소스 ID 색상 적용됨: $categoryColorResId")
                }
                else -> {
                    // ✅ 기본 색상 적용
                    binding.tvCategoryInput.setTextColor(Color.BLACK)
                    Log.e("ExpenseViewFragment", "❌ categoryColor 값이 올바르지 않음!")
                }
            }
        } catch (e: Exception) {
            Log.e("ExpenseViewFragment", "❌ 색상 적용 실패: ${e.message}")
            binding.tvCategoryInput.setTextColor(Color.BLACK) // 기본 색상 적용
        }
    }


    private fun observeLatestHistory() {
        lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.spendingHistoryList.collectLatest { historyList ->
                    if (historyList.isNotEmpty()) {
                        val latestHistory = historyList.last() // 🔹 가장 최근 지출 내역 가져오기
                        updateUiWithHistory(latestHistory)
                    }
                }
            }
        }
    }

    private fun updateUiWithHistory(history: BotSheetUiState.Spending.History) {
        Log.d("ExpenseViewFragment", "updateUiWithHistory - content: '${history.content}'")

        binding.tvDateInput.text = history.date.ifEmpty { getCurrentDate() }
        binding.tvCategoryInput.text = history.categoryName
        binding.tvCategoryInput.setTextColor(Color.parseColor(history.categoryColor))
        binding.etDetailInput.setText(history.name)
        binding.etAmountInput.setText(formatPrice(history.price))

        if (history.photoUrl.isNotEmpty()) {
            Glide.with(binding.root.context) // 🔹 올바른 Context 제공
                .load(history.photoUrl)
                .into(binding.imgPhotoFrame)
            binding.ivPhotoIcon.visibility = View.GONE
        } else {
            binding.ivPhotoIcon.visibility = View.VISIBLE
        }
        if ((history.content ?: "").trim() == "무지출 기록") {
            Log.d("ExpenseViewFragment", "ic_more 숨김 처리")
            binding.icMore.visibility = View.GONE
        } else {
            Log.d("ExpenseViewFragment", "ic_more 표시 처리")
            binding.icMore.visibility = View.VISIBLE
        }
        adjustEditTextWidth(binding.etDetailInput)

    }

    private fun setupUi() {
        // 🔹 제목 텍스트뷰 visibility 변경
        binding.tvExpenseViewTitle.visibility = View.VISIBLE
        binding.tvExpenseTitle.visibility = View.GONE
        binding.btnBack.setOnClickListener {
            saveHistoryToBotSheet() // 수정된 내용 저장
            findNavController().popBackStack()
        }

        lifecycleScope.launch {
            viewModel.spendingHistoryList.collectLatest { historyList ->
                if (historyList.isNotEmpty()) {
                    val latestHistory = historyList.last()
                    updateUiWithHistory(latestHistory)
                }
            }
        }

        arguments?.let {
            val expenseDate = it.getString("expenseDate", getCurrentDate())
            val categoryName = it.getString("categoryName", "")
            val categoryColor = it.getInt("categoryColor", R.color.black2)
            val expenseContent = it.getString("expenseContent", "")
            val expensePrice = it.getInt("expensePrice", 0)
            val expensePhotoUrl = it.getString("expensePhoto", "")

            binding.tvDateInput.text = expenseDate
            binding.tvCategoryInput.text = categoryName
            binding.tvCategoryInput.setTextColor(categoryColor) // Int 값을 바로 사용
            binding.etDetailInput.setText(expenseContent)
            binding.etAmountInput.setText(formatPrice(expensePrice))

            if (expensePhotoUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(expensePhotoUrl)
                    .into(binding.imgPhotoFrame)
                binding.ivPhotoIcon.visibility = View.GONE
            } else {
                binding.ivPhotoIcon.visibility = View.VISIBLE
            }
            adjustEditTextWidth(binding.etDetailInput) // 초기 UI 설정 시에도 높이 조정

        }

        binding.tvDateInput.isEnabled = false
        binding.tvCategoryInput.isEnabled = false
        binding.etDetailInput.isEnabled = false
        binding.etAmountInput.isEnabled = false

        binding.tvDateInput.isFocusable = false
        binding.tvCategoryInput.isFocusable = false
        binding.etDetailInput.isFocusable = false
        binding.etAmountInput.isFocusable = false

        binding.tvCharacterCount.visibility = View.GONE
        binding.tvEntryComplete.visibility = View.GONE
        binding.clExpenseAuto.visibility = View.GONE
        binding.clCheckNoExpense.visibility = View.GONE
        binding.icMore.visibility = View.VISIBLE
        binding.tvEntryComplete.setOnClickListener {
            saveEditedExpense()
        }
        binding.icMore.setOnClickListener { view ->
            showCustomMenu(view)
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy년 M월 d일 E요일", Locale.KOREA)
        return dateFormat.format(Date())
    }

    private fun formatPrice(price: Int): String {
        return NumberFormat.getNumberInstance(Locale.KOREA).format(price)
    }

    private fun saveEditedExpense() {
        // 🔹 수정된 데이터를 저장하는 로직 추가
    }

    // 수정/삭제 커스텀 메뉴 표시
    private fun showCustomMenu(anchor: View) {
        try {
            val categoryName = binding.tvCategoryInput.text.toString()

            val categoryColor = String.format("#%06X", (0xFFFFFF and binding.tvCategoryInput.currentTextColor)) // ✅ HEX 변환

            val popupBinding = ItemMenuPopupBinding.inflate(requireActivity().layoutInflater)
            val popupWindow = PopupWindow(popupBinding.root, 300, 300, true)

            popupBinding.tvModify.setOnClickListener {
                // ✅ 올바른 네비게이션 컨트롤러 확인 후 이동
                val navController = findNavControllerSafely()
                if (navController != null) {
                    val bundle = Bundle().apply {
                        putString("categoryName", categoryName)
                        putString("categoryColor", "#FF5733")
                    }
                    navController.navigate(R.id.action_ExpenseViewFragment_to_ExpenseEditFragment, bundle)
                    Log.d("ExpenseViewFragment", "수정 버튼 클릭됨: categoryName=$categoryName, categoryColor=$categoryColor")
                } else {
                    Log.e("ExpenseViewFragment", "네비게이션 컨트롤러를 찾을 수 없음!")
                }
                popupWindow.dismiss()
            }

            popupBinding.tvDelete.setOnClickListener {
                showDeleteConfirmationDialog()
                popupWindow.dismiss()
            }

            popupWindow.elevation = 10f
            popupWindow.showAsDropDown(anchor, 0, 0)

        } catch (e: Exception) {
            Log.e("ExpenseViewFragment", "showCustomMenu 오류: ${e.message}")
        }
    }

    private fun Fragment.findNavControllerSafely(): NavController? {
        return try {
            findNavController()
        } catch (e: IllegalStateException) {
            Log.e("NavigationError", "NavController를 찾을 수 없음: ${e.message}")
            null
        }
    }

    private fun showDeleteConfirmationDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_expense_delete, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val cancelBtn = dialogView.findViewById<TextView>(R.id.tv_cancel_btn)
        val deleteBtn = dialogView.findViewById<TextView>(R.id.tv_delete_btn)
        val titleTextView = dialogView.findViewById<TextView>(R.id.tv_expense_title)

        val expenseName = binding.etDetailInput.text.toString()
        titleTextView.text = expenseName // 🔥 다이얼로그에 삭제할 항목 표시

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        deleteBtn.setOnClickListener {
            viewModel.removeExpense(expenseName) // ✅ BotSheetViewModel에서 삭제
            Log.d("ExpenseViewFragment", "✅ 삭제 요청: $expenseName")

            Toast.makeText(requireContext(), "지출 내역이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()

            // ✅ 삭제 후 BotSheet 업데이트 트리거
            viewModel.spendingHistoryList.value.let {
                Log.d("ExpenseViewFragment", "삭제 후 바텀시트 업데이트 -> 남은 항목: ${it.size} 개")
            }

            // ✅ 삭제 후 바텀시트 업데이트를 보장하기 위해 데이터 전달
            findNavController().previousBackStackEntry?.savedStateHandle?.set("expenseDeleted", true)

            findNavController().popBackStack() // 이전 화면으로 이동
        }

        dialog.show()
    }



    private fun saveHistoryToBotSheet() {
        val updatedExpense = BotSheetUiState.Spending.History(
            date = binding.tvDateInput.text.toString(),
            categoryName = binding.tvCategoryInput.text.toString(),
            categoryColor = String.format("#%06X", (0xFFFFFF and binding.tvCategoryInput.currentTextColor)), // 색상 HEX 변환
            name = binding.etDetailInput.text.toString(),
            content = binding.etDetailInput.text.toString(),
            price = binding.etAmountInput.text.toString().replace(",", "").toIntOrNull() ?: 0,
            photoUrl = "" // 📝 이미지 추가 필요시 업데이트
        )

        Log.d("ExpenseViewFragment", "saveHistoryToBotSheet called with: $updatedExpense")

        // ✅ ViewModel을 통해 바텀시트에 반영
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun adjustEditTextWidth(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 텍스트 변경 시 넓이 조정
                adjustWidth(editText)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 초기 넓이 조정
        adjustWidth(editText)
    }

    private fun adjustWidth(editText: EditText) {
        val paint = editText.paint
        val width = (paint.measureText(editText.text.toString()) + editText.paddingLeft + editText.paddingRight).toInt()
        editText.width = width
    }
}