package com.example.yeongkkuel.presentation.home.entry

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.yeongkkuel.R
import com.example.yeongkkuel.presentation.botsheet.BotSheetUiState
import com.example.yeongkkuel.presentation.botsheet.BotSheetViewModel
import com.example.yeongkkuel.presentation.util.SpendingCategory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ExpenseEntryFragment : Fragment() {

    private lateinit var navController: NavController
    private lateinit var sharedPreferences: SharedPreferences
    private val PICK_IMAGE_REQUEST = 1

    private val botSheetViewModel: BotSheetViewModel by activityViewModels()

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

        // Bundle에서 카테고리 이름과 색상 데이터를 받음
        val selectedCategory = arguments?.getString("selectedCategory") ?: "기본 카테고리"
        val categoryColor = arguments?.getInt("categoryColor") ?: R.color.black2

        // 카테고리 이름 및 색상 설정
        val tvCategoryInput = view.findViewById<TextView>(R.id.tv_category_input)
        tvCategoryInput.text = selectedCategory
        tvCategoryInput.setTextColor(requireContext().getColor(categoryColor))

        // View 요소 초기화
        val tvDateInput = view.findViewById<TextView>(R.id.tv_date_input)
        val etDetailInput = view.findViewById<EditText>(R.id.et_detail_input)
        val etAmountInput = view.findViewById<EditText>(R.id.et_amount_input)
        val tvCharacterCount = view.findViewById<TextView>(R.id.tv_character_count)
        val flPhotoFrame = view.findViewById<FrameLayout>(R.id.fl_photo_frame)
        val tvEntryComplete = view.findViewById<View>(R.id.tv_entry_complete)
        val btnBack = view.findViewById<ImageView>(R.id.btn_back)
        val ivCircleExpenseUnchecked = view.findViewById<ImageView>(R.id.iv_circle_expense_unchecked)
        val ivCircleExpenseChecked = view.findViewById<ImageView>(R.id.iv_circle_expense_checked)

        // 글자 수 제한 로직 추가
        etDetailInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                tvCharacterCount.text = "$length/24" // 글자 수 업데이트
                if (length > 24) {
                    etDetailInput.error = "최대 24자까지 입력 가능합니다."
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // 지출액 입력란 포맷팅
        etAmountInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val input = s?.toString()?.replace(",", "")?.toLongOrNull() ?: return

                // 최대값 제한
                val limitedValue = if (input > 99_999_999) 99_999_999 else input

                // 천 단위 콤마 추가
                val formatted = String.format("%,d", limitedValue)
                if (formatted != s.toString()) {
                    etAmountInput.removeTextChangedListener(this) // 무한 루프 방지
                    etAmountInput.setText(formatted)
                    etAmountInput.setSelection(formatted.length) // 커서 위치 조정
                    etAmountInput.addTextChangedListener(this)
                }
            }
        })

        // 무지출 체크박스 로직
        ivCircleExpenseUnchecked.setOnClickListener {
            ivCircleExpenseChecked.visibility = View.VISIBLE
            ivCircleExpenseUnchecked.visibility = View.INVISIBLE
            etAmountInput.setText("0") // 지출액 0원 설정
            etAmountInput.isEnabled = false // 지출액 입력 비활성화
        }

        ivCircleExpenseChecked.setOnClickListener {
            ivCircleExpenseChecked.visibility = View.INVISIBLE
            ivCircleExpenseUnchecked.visibility = View.VISIBLE
            etAmountInput.isEnabled = true // 지출액 입력 활성화
            etAmountInput.text.clear() // 지출액 초기화
        }

        // 뒤로가기 버튼 이벤트
        btnBack.setOnClickListener {
            navController.popBackStack()
        }

        // 날짜 초기화
        val today = Calendar.getInstance()
        val year = today.get(Calendar.YEAR)
        val month = today.get(Calendar.MONTH) + 1 // 월은 0부터 시작하므로 +1
        val day = today.get(Calendar.DAY_OF_MONTH)
        val dayOfWeek = getDayOfWeek(year, today.get(Calendar.MONTH), day)
        val formattedDate = getString(R.string.date_format, year, month, day, dayOfWeek)
        tvDateInput.text = formattedDate

        tvDateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val dayOfWeek = getDayOfWeek(selectedYear, selectedMonth, selectedDay)
                    tvDateInput.text = "${selectedYear}년 ${selectedMonth + 1}월 ${selectedDay}일 $dayOfWeek"
                },
                year,
                month,
                day
            )

            datePickerDialog.show()
        }

        // 완료 버튼 클릭 이벤트
        tvEntryComplete.setOnClickListener {
            val detail = etDetailInput.text.toString().trim()
            val amountString = etAmountInput.text.toString().replace(",", "").trim()
            val amount = amountString.toIntOrNull() ?: 0
            val isNoExpenseChecked = ivCircleExpenseChecked.visibility == View.VISIBLE

            // 에러 테두리를 위한 리소스
            val errorBackground = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text_error)
            val normalBackground = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)

            var hasError = false

            // 지출 내용 확인
            if (detail.isBlank() && !isNoExpenseChecked) {
                etDetailInput.background = errorBackground
                hasError = true
            } else {
                etDetailInput.background = normalBackground
            }

            // 지출액 확인
            if (amount <= 0 && !isNoExpenseChecked) {
                etAmountInput.background = errorBackground
                hasError = true
            } else {
                etAmountInput.background = normalBackground
            }

            // 무지출 상태에서 사진만 첨부한 경우 처리
            val isPhotoAttached = sharedPreferences.getString("photoUri", null) != null
            if (isNoExpenseChecked && detail.isBlank() && isPhotoAttached) {
                etDetailInput.background = errorBackground
                hasError = true
            }

            // 에러가 발생하면 저장 로직 중단
            if (hasError) {
                Toast.makeText(requireContext(), "필수 항목을 확인해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ViewModel에 데이터 저장
            val newHistory = BotSheetUiState.Spending.History(
                name = detail,
                price = if (isNoExpenseChecked) 0 else amount
            )

            botSheetViewModel.addExpenseToCategory(
                SpendingCategory.valueOf(selectedCategory.uppercase()),
                newHistory
            )

            // ViewModel에 데이터 추가
            botSheetViewModel.addExpenseToCategory(
                SpendingCategory.valueOf(selectedCategory.uppercase()),
                newHistory
            )

            Toast.makeText(requireContext(), "지출 내역이 저장되었습니다.", Toast.LENGTH_SHORT).show()

            // 화면 이동 처리
            navController.popBackStack() // 홈 화면으로 이동
        }

        // 사진 첨부 버튼 클릭 이벤트
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
                imgPhotoFrame?.setImageURI(uri)
                sharedPreferences.edit().putString("photoUri", uri.toString()).apply()
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
