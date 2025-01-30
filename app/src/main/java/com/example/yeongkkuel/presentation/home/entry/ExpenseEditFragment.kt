package com.example.yeongkkuel.presentation.home.entry

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.yeongkkuel.databinding.FragmentExpenseEntryBinding
import com.example.yeongkkuel.presentation.botsheet.BotSheetUiState
import com.example.yeongkkuel.presentation.botsheet.BotSheetViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpenseEditFragment : Fragment() {

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

        // 🔹 ViewModel 초기화
        viewModel = ViewModelProvider(requireActivity())[BotSheetViewModel::class.java]

        // 🔹 BotSheet의 history 데이터를 감시하고 최신 내역 반영
        observeLatestHistory()

        // 🔹 UI 초기 데이터 설정
        setupUi()
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
        binding.tvExpenseEditTitle.visibility = View.VISIBLE
        binding.tvExpenseTitle.visibility = View.GONE
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

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

        binding.icMore.visibility = View.GONE
        binding.clExpenseAuto.visibility = View.GONE
        binding.clCheckNoExpense.visibility = View.GONE
        binding.tvCharacterCount.visibility = View.GONE
        binding.tvEntryComplete.setOnClickListener {
            saveEditedExpense()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
