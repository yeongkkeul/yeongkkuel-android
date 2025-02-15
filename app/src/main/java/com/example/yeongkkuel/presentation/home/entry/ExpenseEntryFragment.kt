package com.example.yeongkkuel.presentation.home.entry

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.yeongkkuel.R
import com.example.yeongkkuel.network.RetrofitClient
import com.example.yeongkkuel.presentation.botsheet.BotSheetUiState
import com.example.yeongkkuel.presentation.botsheet.BotSheetViewModel
import com.example.yeongkkuel.presentation.home.entry.data.ExpenseRepository
import com.example.yeongkkuel.presentation.home.entry.data.ExpenseRequest
import com.example.yeongkkuel.presentation.home.entry.data.ExpenseViewModel
import com.example.yeongkkuel.presentation.util.SpendingCategory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ExpenseEntryFragment : Fragment() {

    private lateinit var navController: NavController
    private lateinit var sharedPreferences: SharedPreferences
    private val PICK_IMAGE_REQUEST = 1
    private var expenseDate: String = ""
    private var expensePhotoUrl: String = ""

    private val botSheetViewModel: BotSheetViewModel by activityViewModels()
    private lateinit var expenseViewModel: ExpenseViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_expense_entry, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        sharedPreferences = requireContext().getSharedPreferences("ExpensePrefs", Context.MODE_PRIVATE)

        val repository = ExpenseRepository(RetrofitClient.expenseApiService)
        expenseViewModel = ViewModelProvider(this, ExpenseViewModel.Factory(repository)).get(ExpenseViewModel::class.java)


        setupCategory(view)
        initializeViews(view)
        setupDatePicker(view)
        setupDetailInput(view)
        setupAmountInput(view)
        setupExpenseCheckbox(view)
        setupAutoSendCheckbox(view)
        setupBackButton(view)
        setupCompleteButton(view)
        setupPhotoFrame(view)
    }

    // 카테고리 관련 초기화
    private fun setupCategory(view: View) {
        val selectedCategory = arguments?.getString("selectedCategory") ?: "기본 카테고리"
        val categoryColor = arguments?.getInt("categoryColor") ?: R.color.black2
        val tvCategoryInput = view.findViewById<TextView>(R.id.tv_category_input)
        tvCategoryInput.text = selectedCategory

        Log.d("ExpenseEntryFragment", "setupCategory - selectedCategory: $selectedCategory, categoryColor: $categoryColor")

        tvCategoryInput.setTextColor(requireContext().getColor(categoryColor))
    }

    // 뷰 초기화
    private fun initializeViews(view: View) {
        val today = Calendar.getInstance()
        val year = today.get(Calendar.YEAR)
        val month = today.get(Calendar.MONTH) + 1 // 월은 0부터 시작하므로 +1
        val day = today.get(Calendar.DAY_OF_MONTH)
        val dayOfWeek = getDayOfWeek(year, today.get(Calendar.MONTH), day)
        val formattedDate = getString(R.string.date_format, year, month, day, dayOfWeek)
        view.findViewById<TextView>(R.id.tv_date_input).text = formattedDate
    }

    // 날짜 선택기 설정
    private fun setupDatePicker(view: View) {
        val tvDateInput = view.findViewById<TextView>(R.id.tv_date_input)
        tvDateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val dayOfWeek = getDayOfWeek(selectedYear, selectedMonth, selectedDay)
                    expenseDate= "${selectedYear}년 ${selectedMonth + 1}월 ${selectedDay}일 $dayOfWeek"
                    tvDateInput.text = expenseDate

                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
    }

    // 상세 입력란 글자 수 제한 로직
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

    // 지출액 입력란 포맷팅
    private fun setupAmountInput(view: View) {
        val etAmountInput = view.findViewById<EditText>(R.id.et_amount_input)

        etAmountInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                etAmountInput.removeTextChangedListener(this)

                val rawInput = s?.toString()?.replace(",", "") ?: ""
                val input = rawInput.toLongOrNull() ?: 0

                // 🔹 최대 8자리까지만 입력 가능하도록 제한
                val trimmedInput = if (rawInput.length > 8) rawInput.substring(0, 8) else rawInput
                val limitedValue = trimmedInput.toLongOrNull()?.coerceAtMost(99_999_999) ?: 0

                val formatted = String.format("%,d", limitedValue)

                if (formatted != s.toString()) {
                    etAmountInput.setText(formatted)
                    etAmountInput.setSelection(formatted.length)
                }

                etAmountInput.addTextChangedListener(this)
            }
        })
    }

    // 무지출 체크박스 로직
    private fun setupExpenseCheckbox(view: View) {
        val ivUnchecked = view.findViewById<ImageView>(R.id.iv_circle_expense_unchecked)
        val ivChecked = view.findViewById<ImageView>(R.id.iv_circle_expense_checked)
        val etAmountInput = view.findViewById<EditText>(R.id.et_amount_input)

        ivUnchecked.setOnClickListener {
            ivChecked.visibility = View.VISIBLE
            ivUnchecked.visibility = View.INVISIBLE
            etAmountInput.setText("0")
            etAmountInput.isEnabled = false
        }

        ivChecked.setOnClickListener {
            ivChecked.visibility = View.INVISIBLE
            ivUnchecked.visibility = View.VISIBLE
            etAmountInput.isEnabled = true
            etAmountInput.text.clear()
        }
    }

    // 채팅방 자동 전송 체크박스 로직
    private fun setupAutoSendCheckbox(view: View) {
        val ivUnchecked = view.findViewById<ImageView>(R.id.iv_circle_send_auto_unchecked)
        val ivChecked = view.findViewById<ImageView>(R.id.iv_circle_send_auto_checked)

        // 초기 상태를 체크 상태로 설정
        ivChecked.visibility = View.VISIBLE
        ivUnchecked.visibility = View.INVISIBLE

        ivUnchecked.setOnClickListener {
            ivChecked.visibility = View.VISIBLE
            ivUnchecked.visibility = View.INVISIBLE
        }

        ivChecked.setOnClickListener {
            ivChecked.visibility = View.INVISIBLE
            ivUnchecked.visibility = View.VISIBLE
        }
    }

    // 뒤로가기 버튼 로직
    private fun setupBackButton(view: View) {
        val btnBack = view.findViewById<ImageView>(R.id.btn_back)
        btnBack.setOnClickListener {
            navController.popBackStack()
        }
    }

    // 완료 버튼 로직
    private fun setupCompleteButton(view: View) {
        val tvEntryComplete = view.findViewById<View>(R.id.tv_entry_complete)
        tvEntryComplete.setOnClickListener {
            if (validateAndSaveEntry(view)) {
                saveExpense(view) // API 요청 및 저장
            }
        }
    }
    private fun saveExpense(view: View) {
        val tvDateInput = view.findViewById<TextView>(R.id.tv_date_input)
        val etDetailInput = view.findViewById<EditText>(R.id.et_detail_input)
        val etAmountInput = view.findViewById<EditText>(R.id.et_amount_input)
        val ivCircleExpenseChecked = view.findViewById<ImageView>(R.id.iv_circle_expense_checked)
        val ivCircleSendChecked = view.findViewById<ImageView>(R.id.iv_circle_send_auto_checked)

        val detail = etDetailInput.text.toString().trim()
        val amountString = etAmountInput.text.toString().replace(",", "").trim()
        val amount = amountString.toIntOrNull() ?: 0
        val isNoExpenseChecked = ivCircleExpenseChecked.visibility == View.VISIBLE // ✅ 무지출 체크 여부 확인
        val isSendChatRoomChecked = ivCircleSendChecked.visibility == View.VISIBLE


        // 유효한 카테고리 ID를 가져옴 (API 요청 오류 방지)
        val selectedCategoryName = arguments?.getString("selectedCategory") ?: "기본 카테고리"
        val matchingCategory = botSheetViewModel.uiState.value.spendingList.find {
            it.kind.name.equals(selectedCategoryName, ignoreCase = true)
        }
        if(matchingCategory == null) {
            Log.e("ExpenseEntryFragment", "선택된 카테고리에 해당하는 항목이 없습니다.")
            Toast.makeText(requireContext(), "사용 가능한 카테고리가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        val validCategoryId = matchingCategory.categoryId


        if (validCategoryId == -1) {
            Toast.makeText(requireContext(), "사용 가능한 카테고리가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val expenseRequest = ExpenseRequest(
            day = formatDateForServer(tvDateInput.text.toString()), // 날짜 포맷 변환
            categoryId = validCategoryId, // 유효한 카테고리 ID
            content = detail,
            amount = if (isNoExpenseChecked) 0 else amount,
            isExpense = isNoExpenseChecked,
            expenseImg = expensePhotoUrl.takeIf { it.isNotEmpty() },
            sendChatRoom = isSendChatRoomChecked
        )

        Log.d("ExpenseEntryFragment", "지출 내역 요청 데이터: $expenseRequest")
        expenseViewModel.createExpense(expenseRequest) { response ->
            if (response?.isSuccess == true) {
                Log.d("ExpenseEntryFragment", "지출 내역 저장 완료: ${response.result}")
                Toast.makeText(requireContext(), "지출 내역이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                navController.navigate(R.id.navigation_home)
            } else {
                Log.e("ExpenseEntryFragment", "지출 내역 저장 실패: ${response?.message}")
                Toast.makeText(requireContext(), "지출 내역 저장 실패.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun formatDateForServer(date: String): String {
        val regex = """(\d{4})년 (\d{1,2})월 (\d{1,2})일""".toRegex()
        val matchResult = regex.find(date)
        return matchResult?.let {
            val (year, month, day) = it.destructured
            "%04d-%02d-%02d".format(year.toInt(), month.toInt(), day.toInt())
        } ?: SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN).format(Date())
    }


    private fun validateAndSaveEntry(view: View): Boolean {
        val clDetailInput = view.findViewById<ConstraintLayout>(R.id.cl_detail_input) // ✅ 부모 ConstraintLayout
        val etDetailInput = view.findViewById<EditText>(R.id.et_detail_input)
        val etAmountInput = view.findViewById<EditText>(R.id.et_amount_input)
        val ivCircleExpenseChecked = view.findViewById<ImageView>(R.id.iv_circle_expense_checked)

        val detail = etDetailInput.text.toString().trim()
        val amountString = etAmountInput.text.toString().replace(",", "").trim()
        val amount = amountString.toIntOrNull() ?: 0
        val isNoExpenseChecked = ivCircleExpenseChecked.visibility == View.VISIBLE

        val errorBackground = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text_error)
        val normalBackground = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)

        var hasError = false

        // 지출 내용 확인
        if (detail.isBlank() && !isNoExpenseChecked) {
            clDetailInput.setBackgroundResource(R.drawable.bg_edit_text_error) // ✅ 부모 배경 변경
            etDetailInput.setBackgroundResource(android.R.color.transparent)  // ✅ EditText 배경 투명하게
            hasError = true
            Toast.makeText(requireContext(), "지출 내용을 입력하세요.", Toast.LENGTH_SHORT).show()
        } else {
            clDetailInput.setBackgroundResource(R.drawable.bg_edit_text) // ✅ 부모 배경 원래대로
            etDetailInput.setBackgroundResource(android.R.color.transparent)
        }

        // 지출액 확인
        if (amount <= 0 && !isNoExpenseChecked) {
            etAmountInput.background = errorBackground
            hasError = true
            Toast.makeText(requireContext(), "지출액을 입력하세요.", Toast.LENGTH_SHORT).show()
        } else {
            etAmountInput.background = normalBackground
        }

        // 에러 발생 시 저장 로직 중단
        if (hasError) {
            return false
        }

        // ViewModel에 저장
        val selectedCategory = arguments?.getString("selectedCategory") ?: "기타"
        // 선택된 카테고리 값 확인
        Log.d("ExpenseEntryFragment", "validateAndSaveEntry - selectedCategory: '$selectedCategory'")
        val expenseHistory = BotSheetUiState.Spending.History(
            id = 1,
            name =  detail,
            price = if (isNoExpenseChecked) 0 else amount,
            imgExist = false
        )

        botSheetViewModel.addExpenseHistory(expenseHistory)


        // SpendingCategory 처리
        return try {
            val categoryEnum = SpendingCategory.fromName(selectedCategory)
            Log.d("ExpenseEntryFragment", "SpendingCategory.fromName 결과: $categoryEnum")
            botSheetViewModel.addExpenseToCategory(categoryEnum, expenseHistory)
            Toast.makeText(requireContext(), "지출 내역이 저장되었습니다.", Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "카테고리가 유효하지 않습니다.", Toast.LENGTH_SHORT).show()
            Log.e("ExpenseEntryFragment", "Error saving data: ${e.message}", e)
            false
        }

    }

    private fun handleNavigationAfterSave(view: View) {
        val tvDateInput = view.findViewById<TextView>(R.id.tv_date_input)

        val selectedDateText = tvDateInput.text.toString()
        val today = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREAN)

        val selectedDate = try {
            dateFormat.parse(selectedDateText.substring(0, 13))
        } catch (e: Exception) {
            null
        }

        if (selectedDate != null && dateFormat.format(selectedDate) != dateFormat.format(today.time)) {
            // Bundle 생성 및 데이터 추가
            val bundle = Bundle().apply {
                putInt("selected_tab_index", 2) // 월간 탭(인덱스 2) 지정
            }

            // StatFragment로 이동하며 Bundle 전달
            navController.navigate(R.id.action_expenseEntryFragment_to_navigation_stat, bundle)
        } else {
            navController.navigate(R.id.navigation_home)
        }
    }

    // 사진 첨부 버튼 로직
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
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK) {
            data?.data?.let { uri ->
                val imgPhotoFrame = view?.findViewById<ImageView>(R.id.img_photo_frame)
                val ivPhotoIcon = view?.findViewById<ImageView>(R.id.iv_photo_icon)
                imgPhotoFrame?.setImageURI(uri)
                ivPhotoIcon?.visibility = View.GONE
                expensePhotoUrl = uri.toString()
            }
        }
    }

    private fun getDayOfWeek(year: Int, month: Int, day: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "일요일"
            Calendar.MONDAY -> "월요일"
            Calendar.TUESDAY -> "화요일"
            Calendar.WEDNESDAY -> "수요일"
            Calendar.THURSDAY -> "목요일"
            Calendar.FRIDAY -> "금요일"
            Calendar.SATURDAY -> "토요일"
            else -> ""
        }
    }


}