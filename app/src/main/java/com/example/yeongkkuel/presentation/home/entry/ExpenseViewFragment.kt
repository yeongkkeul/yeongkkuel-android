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
        val categoryColor = arguments?.getInt("categoryColor") ?: R.color.black2

        Log.d("ExpenseViewFragment", "setupCategory - categoryName: $selectedCategory, categoryColor: $categoryColor")

        binding.tvCategoryInput.text = selectedCategory
        binding.tvCategoryInput.setTextColor(categoryColor) // Int 값을 바로 사용
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
        val categoryName = binding.tvCategoryInput.text.toString()
        val categoryColor = binding.tvCategoryInput.currentTextColor // 현재 색상을 Int 값으로 가져옴

        val popupBinding = ItemMenuPopupBinding.inflate(layoutInflater)
        val popupWindow = PopupWindow(popupBinding.root, 300, 300, true)

        popupBinding.tvModify.setOnClickListener {
            // 수정 화면으로 이동
            val bundle = Bundle().apply {
                putString("categoryName", categoryName)
                putInt("categoryColor", categoryColor)
            }
            findNavController().navigate(
                R.id.action_ExpenseViewFragment_to_ExpenseEditFragment, // ✅ 수정 화면으로 이동
                bundle
            )
            popupWindow.dismiss()
        }

        popupBinding.tvDelete.setOnClickListener {
            // 삭제 확인 다이얼로그 표시
            showDeleteConfirmationDialog() // ✅ 인자 없이 호출
            popupWindow.dismiss()
        }

        popupWindow.elevation = 10f
        popupWindow.showAsDropDown(anchor, 0, 0) // 앵커 기준으로 표시
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