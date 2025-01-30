package com.example.yeongkkuel.presentation.home.entry

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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

    private lateinit var viewModel: BotSheetViewModel  // 🔹 ViewModel 추가

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

        Log.d("ExpenseViewFragment", "onViewCreated - arguments: $arguments")

        viewModel = ViewModelProvider(requireActivity())[BotSheetViewModel::class.java]

        // ✅ 최신 내역을 감시하여 UI 업데이트
        observeLatestHistory()

        // ✅ UI 초기 데이터 설정
        setupUi()
        setupCategory()

        binding.icMore.setOnClickListener { view ->
            showCustomMenu(view)
        }
    }

    private fun setupCategory() {
        val selectedCategory = arguments?.getString("categoryName") ?: "기본 카테고리"
        val categoryColor = arguments?.getString("categoryColor") ?: "#000000"

        Log.d("ExpenseViewFragment", "setupCategory - categoryName: $selectedCategory, categoryColor: $categoryColor")

        binding.tvCategoryInput.text = selectedCategory
        try {
            binding.tvCategoryInput.setTextColor(Color.parseColor(categoryColor)) // HEX 색상 적용
        } catch (e: IllegalArgumentException) {
            Log.e("ExpenseViewFragment", "Invalid categoryColor: $categoryColor", e)
            binding.tvCategoryInput.setTextColor(Color.BLACK) // 기본값 검정색
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
    }

    private fun setupUi() {
        // 🔹 제목 텍스트뷰 visibility 변경
        binding.tvExpenseViewTitle.visibility = View.VISIBLE
        binding.tvExpenseTitle.visibility = View.GONE
        binding.btnBack.setOnClickListener {
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
            binding.tvDateInput.text = it.getString("expenseDate", getCurrentDate())
            binding.tvCategoryInput.text = it.getString("categoryName", "")
            binding.tvCategoryInput.setTextColor(Color.parseColor(it.getString("categoryColor", "#000000")))
            binding.etDetailInput.setText(it.getString("expenseContent", ""))
            binding.etAmountInput.setText(formatPrice(it.getInt("expensePrice", 0)))

            val expensePhotoUrl = it.getString("expensePhoto", "")
            if (expensePhotoUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(expensePhotoUrl)
                    .into(binding.imgPhotoFrame)
                binding.ivPhotoIcon.visibility = View.GONE
            } else {
                binding.ivPhotoIcon.visibility = View.VISIBLE
            }
        }
        setupCategory()

        // 🔹 기존 Bundle 데이터 처리
        val expenseDate = arguments?.getString("expenseDate") ?: getCurrentDate() // ✅ 오늘 날짜 기본값 설정
        val categoryName = arguments?.getString("categoryName") ?: ""
        val categoryColor = arguments?.getString("categoryColor") ?: "#000000" // ✅ 기본 색상 적용
        val expenseContent = arguments?.getString("expenseContent") ?: ""
        val expensePrice = arguments?.getInt("expensePrice") ?: 0
        val expensePhotoUrl = arguments?.getString("expensePhoto") ?: ""

        binding.tvDateInput.text = expenseDate
        binding.tvCategoryInput.text = categoryName
        binding.tvCategoryInput.setTextColor(Color.parseColor(categoryColor))
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
        val popupMenu = PopupMenu(requireContext(), anchor)
        popupMenu.menuInflater.inflate(R.menu.menu_edit_delete, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_modify -> {
                    val categoryColor = String.format("#%06X", (0xFFFFFF and binding.tvCategoryInput.currentTextColor))

                    val bundle = Bundle().apply {
                        putString("expenseDate", binding.tvDateInput.text.toString())
                        putString("categoryName", binding.tvCategoryInput.text.toString())
                        putString("categoryColor", categoryColor) // ✅ HEX 코드로 변환한 색상 값 전달
                        putString("expenseContent", binding.etDetailInput.text.toString())
                        putInt("expensePrice", binding.etAmountInput.text.toString().replace(",", "").toIntOrNull() ?: 0)
                        putString("expensePhoto", "") // 필요하면 photo URL 추가
                    }
                    Log.d("ExpenseViewFragment", "showCustomMenu - categoryColor: $categoryColor")
                    findNavController().navigate(R.id.navigation_expense_edit, bundle)
                    true
                }
                R.id.action_delete -> {
                    // ✅ 삭제 확인 다이얼로그 표시
                    showDeleteConfirmationDialog()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
    private fun showDeleteConfirmationDialog() {
        // ✅ 다이얼로그 뷰 inflate
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_expense_delete, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // ✅ 다이얼로그 내 버튼 참조
        val cancelBtn = dialogView.findViewById<TextView>(R.id.tv_cancel_btn)
        val deleteBtn = dialogView.findViewById<TextView>(R.id.tv_delete_btn) // ✅ dialog_expense_delete의 deleteBtn

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        deleteBtn.setOnClickListener {
            // ✅ ViewModel을 통해 해당 내역 삭제
            val expenseName = binding.etDetailInput.text.toString()
            viewModel.removeExpense(expenseName)

            Toast.makeText(requireContext(), "지출 내역이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()

            // ✅ 삭제 후 이전 화면으로 이동
            findNavController().popBackStack()
        }
        dialog.window?.apply {
            setBackgroundDrawableResource(R.drawable.ic_store_topurchase) // VectorDrawable 설정
            decorView.clipToOutline = true // 💡 둥근 모서리 적용
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}