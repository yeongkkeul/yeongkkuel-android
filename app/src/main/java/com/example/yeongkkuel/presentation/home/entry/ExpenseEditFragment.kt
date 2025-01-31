package com.example.yeongkkuel.presentation.home.entry

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
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
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var viewModel: BotSheetViewModel  // 🔹 ViewModel 추가
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null // 🔹 선택한 이미지 URI 저장

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

        viewModel = ViewModelProvider(requireActivity())[BotSheetViewModel::class.java]
        observeLatestHistory()
        setupUi()
        binding.tvEntryComplete.setOnClickListener {
            saveEditedExpense()
        }

        sharedPreferences = requireContext().getSharedPreferences("ExpensePrefs", Context.MODE_PRIVATE)

        setupDetailInput(view)
        setupAmountInput(view)
        setupPhotoFrame(view)
    }

    private fun setupPhotoFrame(view: View) {
        val flPhotoFrame = view.findViewById<FrameLayout>(R.id.fl_photo_frame)
        flPhotoFrame.setOnClickListener {
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*" // 이미지 파일만 선택
        }
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            selectedImageUri?.let {
                Glide.with(this)
                    .load(it)
                    .into(binding.imgPhotoFrame)
                binding.ivPhotoIcon.visibility = View.GONE
            }
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
        val updatedExpense = BotSheetUiState.Spending.History(
            date = binding.tvDateInput.text.toString(),
            categoryName = binding.tvCategoryInput.text.toString(),
            categoryColor = String.format("#%06X", (0xFFFFFF and binding.tvCategoryInput.currentTextColor)), // 색상 HEX 변환
            name = binding.etDetailInput.text.toString(),
            content = binding.etDetailInput.text.toString(),
            price = binding.etAmountInput.text.toString().replace(",", "").toIntOrNull() ?: 0,
            photoUrl = selectedImageUri?.toString() ?: ""
        )

        // 🔹 ViewModel을 통해 지출 내역 업데이트
        viewModel.updateExpense(updatedExpense)
        viewModel.updateBotSheetHistory(updatedExpense)

        val bundle = Bundle().apply {
            putString("expenseDate", updatedExpense.date)
            putString("categoryName", updatedExpense.categoryName)
            putString("categoryColor", updatedExpense.categoryColor)
            putString("expenseContent", updatedExpense.content)
            putInt("expensePrice", updatedExpense.price)
            putString("expensePhoto", updatedExpense.photoUrl)
        }

        findNavController().previousBackStackEntry?.savedStateHandle?.set("editedExpense", bundle)

        Toast.makeText(requireContext(), "지출 내역이 수정되었습니다.", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    private fun setupDetailInput(view: View) {
        val etDetailInput = view.findViewById<EditText>(R.id.et_detail_input)
        val tvCharacterCount = view.findViewById<TextView>(R.id.tv_character_count)

        etDetailInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                tvCharacterCount.text = "$length/24"
                if (length > 24) etDetailInput.error = "최대 24자까지 입력 가능합니다."
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupAmountInput(view: View) {
        val etAmountInput = view.findViewById<EditText>(R.id.et_amount_input)

        etAmountInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val input = s?.toString()?.replace(",", "")?.toLongOrNull() ?: return
                val limitedValue = if (input > 99_999_999) 99_999_999 else input
                val formatted = String.format("%,d", limitedValue)
                if (formatted != s.toString()) {
                    etAmountInput.removeTextChangedListener(this)
                    etAmountInput.setText(formatted)
                    etAmountInput.setSelection(formatted.length)
                    etAmountInput.addTextChangedListener(this)
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}