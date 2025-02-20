package com.example.yeongkkuel.presentation.home.entry

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.example.yeongkkuel.R
import com.example.yeongkkuel.network.RetrofitClient
import com.example.yeongkkuel.presentation.botsheet.BotSheetListener
import com.example.yeongkkuel.presentation.botsheet.BotSheetUiState
import com.example.yeongkkuel.presentation.botsheet.BotSheetViewModel
import com.example.yeongkkuel.presentation.home.entry.data.*
import com.example.yeongkkuel.presentation.stat.StatFragment
import com.example.yeongkkuel.presentation.util.SpendingCategory
import com.example.yeongkkuel.presentation.util.dpToPx
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ExpenseEntryFragment : Fragment() {

    private val expenseViewModel: ExpenseViewModel by activityViewModels()
    private val botSheetViewModel: BotSheetViewModel by activityViewModels()
    private var selectedImageUri: Uri? = null
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var navController: androidx.navigation.NavController
    private val PICK_IMAGE_REQUEST = 1
    private var expenseDate: String = ""

    private var botSheetListener: BotSheetListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is BotSheetListener) {
            botSheetListener = context
        }
    }

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
        sharedPreferences =
            requireContext().getSharedPreferences("ExpensePrefs", Context.MODE_PRIVATE)

        val repository = ExpenseRepository(RetrofitClient.expenseApiService)

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

    // ✅ 완료 버튼 클릭 시 데이터 저장 및 API 호출
    private fun setupCompleteButton(view: View) {
        val tvEntryComplete = view.findViewById<TextView>(R.id.tv_entry_complete)
        tvEntryComplete.setOnClickListener {
            if (validateAndSaveEntry(view)) {
                saveExpense(view)
            }
        }
    }

    // 카테고리 관련 초기화
    private fun setupCategory(view: View) {
        val selectedCategory = arguments?.getString("selectedCategory") ?: "기본 카테고리"
        val categoryColor = arguments?.getInt("categoryColor") ?: R.color.black2
        val tvCategoryInput = view.findViewById<TextView>(R.id.tv_category_input)
        tvCategoryInput.text = selectedCategory
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
                    expenseDate =
                        "${selectedYear}년 ${selectedMonth + 1}월 ${selectedDay}일 $dayOfWeek"
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

    private fun saveExpense(view: View) {
        val tvDateInput = view.findViewById<TextView>(R.id.tv_date_input)
        val etDetailInput = view.findViewById<EditText>(R.id.et_detail_input)
        val etAmountInput = view.findViewById<EditText>(R.id.et_amount_input)
        val ivCircleExpenseChecked = view.findViewById<ImageView>(R.id.iv_circle_expense_checked)
        val ivCircleSendChecked = view.findViewById<ImageView>(R.id.iv_circle_send_auto_checked)

        val detail = etDetailInput.text.toString().trim()
        val amountString = etAmountInput.text.toString().replace(",", "").trim()
        val amount = amountString.toIntOrNull() ?: 0
        val isNoExpenseChecked = ivCircleExpenseChecked.visibility == View.VISIBLE
        val isSendChatRoomChecked = ivCircleSendChecked.visibility == View.VISIBLE

        val formattedDate = formatDateForServer(tvDateInput.text.toString())

        // 🔹 선택된 이미지 파일을 `MultipartBody.Part`로 변환
        val imagePart = selectedImageUri?.let { uri ->
            try {
                requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                    val tempFile = File.createTempFile("upload", ".jpg", requireContext().cacheDir)
                    tempFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }

                    val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("expenseImage", tempFile.name, requestFile)
                }
            } catch (e: Exception) {
                Log.e("ExpenseEntryFragment", "🚨 이미지 변환 실패: ${e.message}")
                null
            }
        }
        val selectedCategoryName = arguments?.getString("selectedCategory") ?: "기본 카테고리"
        val matchingCategory = botSheetViewModel.uiState.value.spendingList.find {
            it.kind.name.equals(selectedCategoryName, ignoreCase = true)
        } ?: run {
            Toast.makeText(requireContext(), "사용 가능한 카테고리가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val validCategoryId = matchingCategory.categoryId
        if (validCategoryId == -1) {
            Toast.makeText(requireContext(), "사용 가능한 카테고리가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val expenseRequest = ExpenseRequest(
            day = formattedDate,
            categoryId = validCategoryId,
            content = detail,
            amount = if (isNoExpenseChecked) 0 else amount,
            isExpense = isNoExpenseChecked,
            sendChatRoom = isSendChatRoomChecked
        )


        // ✅ API 호출
        viewLifecycleOwner.lifecycleScope.launch {
            expenseViewModel.createExpense(expenseRequest, imagePart) { response ->
                if (response?.isSuccess == true) {
                    Toast.makeText(requireContext(), "지출 내역이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                    val dateText = tvDateInput.text.toString() // 예: "2025년 2월 19일"

                    // 정규식으로 연, 월, 일을 추출
                    val regex = "(\\d{4})년 (\\d{1,2})월 (\\d{1,2})일".toRegex()
                    val matchResult = regex.find(dateText)

                    if (matchResult != null) {
                        val (year, month, day) = matchResult.destructured
                        val yearInt = year.toInt()
                        val monthInt = month.toInt()
                        val dayInt = day.toInt()

                        botSheetViewModel.getSpendingList(
                            year = yearInt,
                            month = monthInt,
                            day = dayInt
                        )
                    } else botSheetViewModel.getSpendingList()

                    navigateAfterSavingExpense()
                } else {
                    Toast.makeText(requireContext(), "지출 내역 저장 실패.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validateAndSaveEntry(view: View): Boolean {
        val clDetailInput = view.findViewById<ConstraintLayout>(R.id.cl_detail_input)
        val etDetailInput = view.findViewById<EditText>(R.id.et_detail_input)
        val etAmountInput = view.findViewById<EditText>(R.id.et_amount_input)
        val ivCircleExpenseChecked = view.findViewById<ImageView>(R.id.iv_circle_expense_checked)

        val detail = etDetailInput.text.toString().trim()
        val amountString = etAmountInput.text.toString().replace(",", "").trim()
        val amount = amountString.toIntOrNull() ?: 0
        val isNoExpenseChecked = ivCircleExpenseChecked.visibility == View.VISIBLE

        val errorBackground =
            ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text_error) // 지출 내용 에러
        val errorBackground2 =
            ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text_error2) // 지출액 에러
        val normalBackground = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)

        var hasError = false

        // 지출 내용 확인
        if (detail.isBlank() && !isNoExpenseChecked) {
            clDetailInput.background = errorBackground
            etDetailInput.setBackgroundResource(android.R.color.transparent)
            hasError = true
            Toast.makeText(requireContext(), "지출 내용을 입력하세요.", Toast.LENGTH_SHORT).show()
        } else {
            clDetailInput.background = normalBackground
            etDetailInput.setBackgroundResource(android.R.color.transparent)
        }

        // 지출액 확인
        if (amount <= 0 && !isNoExpenseChecked) {
            etAmountInput.background = errorBackground2
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
        val expenseHistory = BotSheetUiState.Spending.History(
            id = 1,
            name = detail,
            price = if (isNoExpenseChecked) 0 else amount,
            imgExist = selectedImageUri?.toString() ?: ""
        )

        botSheetViewModel.addExpenseHistory(expenseHistory)

        // SpendingCategory 처리
        return try {
            val categoryEnum = SpendingCategory.fromName(selectedCategory)
            botSheetViewModel.addExpenseToCategory(categoryEnum, expenseHistory)
            Toast.makeText(requireContext(), "지출 내역이 저장되었습니다.", Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "카테고리가 유효하지 않습니다.", Toast.LENGTH_SHORT).show()
            false
        }

    }

    private fun navigateAfterSavingExpense() {
        val fromTab = arguments?.getString("fromTab") ?: "home" // 기본값은 홈 탭
        val tvDateInput = view?.findViewById<TextView>(R.id.tv_date_input)

        val selectedDateText = tvDateInput?.text.toString()
        val today = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)

        val selectedDate = try {
            dateFormat.parse(formatDateForServer(selectedDateText))
        } catch (e: Exception) {
            null
        }

        if (dateFormat.format(selectedDate) != dateFormat.format(today.time)) {
            // 날짜가 다르면 지출 탭의 월간 탭으로 이동

            val bundle = Bundle().apply {
                putInt("selected_tab_index", 2) // ✅ 월간 탭의 인덱스 2
            }
            navController.navigate(R.id.navigation_stat, bundle) // ✅ 지출 탭 이동

            val statFragment =
                parentFragmentManager.findFragmentById(R.id.fragment_container) as? StatFragment
            statFragment?.moveToMonthlyTab()

        } else {
            // 기존 로직 유지 (홈 or 지출 탭으로 이동)
            when (fromTab) {
                "home" -> navController.navigate(R.id.navigation_home)
                "stat" -> navController.navigate(R.id.navigation_stat)
                else -> navController.navigate(R.id.navigation_home)
            }
        }

        // 바텀시트 높이를 원래대로 복귀
        val displayHeight = resources.displayMetrics.heightPixels
        val peekHeight = (displayHeight - 528.dpToPx(requireContext()))
        botSheetListener?.setPeekHeight(peekHeight)
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

    // 🔹 날짜 포맷 변환 함수 (YYYY-MM-DD로 변경)
    private fun formatDateForServer(date: String): String {
        val regex = """(\d{4})년 (\d{1,2})월 (\d{1,2})일""".toRegex()
        val matchResult = regex.find(date)
        return matchResult?.let {
            val (year, month, day) = it.destructured
            "%04d-%02d-%02d".format(year.toInt(), month.toInt(), day.toInt())
        } ?: SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN).format(Date())
    }

    // 갤러리에서 이미지 선택
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
                selectedImageUri = uri // ✅ 선택한 이미지 URI 저장

                val imgPhotoFrame = view?.findViewById<ImageView>(R.id.img_photo_frame)
                val ivPhotoIcon = view?.findViewById<ImageView>(R.id.iv_photo_icon)

                Glide.with(this)
                    .load(uri)
                    .override(500, 500) // ✅ 크기 조정
                    .centerCrop() // ✅ 중앙 정렬하여 크기 맞춤
                    .into(imgPhotoFrame!!) // ✅ 둥근 모서리는 XML에서 처리

                ivPhotoIcon?.visibility = View.GONE // 아이콘 숨김
            }
        }
    }

//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
}