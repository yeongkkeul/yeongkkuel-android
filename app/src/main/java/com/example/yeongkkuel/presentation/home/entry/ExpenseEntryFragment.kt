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
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.fragment.app.activityViewModels
import com.example.yeongkkuel.R
import com.example.yeongkkuel.presentation.botsheet.BotSheetViewModel
import com.example.yeongkkuel.presentation.botsheet.BotSheetUiState
import com.example.yeongkkuel.presentation.util.SpendingCategory
import java.util.Calendar

class ExpenseEntryFragment : Fragment() {

    private lateinit var navController: NavController
    private lateinit var sharedPreferences: SharedPreferences
    private val PICK_IMAGE_REQUEST = 1

    // BotSheetViewModel 연결
    private val botSheetViewModel: BotSheetViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 레이아웃 파일을 인플레이트
        return inflater.inflate(R.layout.fragment_expense_entry, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tvCategoryInput = view.findViewById<TextView>(R.id.tv_category_input) // 추가한 TextView 바인딩

        navController = Navigation.findNavController(view)
        sharedPreferences =
            requireContext().getSharedPreferences("ExpensePrefs", Context.MODE_PRIVATE)

        val btnBack = view.findViewById<ImageView>(R.id.btn_back)
        val rbNoExpense = view.findViewById<RadioButton>(R.id.rb_no_expense)
        val etDateInput = view.findViewById<EditText>(R.id.et_date_input)
        val etDetailInput = view.findViewById<EditText>(R.id.et_detail_input)
        val etAmountInput = view.findViewById<EditText>(R.id.et_amount_input)
        val flPhotoFrame = view.findViewById<FrameLayout>(R.id.fl_photo_frame)
        val rbAutoSendChat = view.findViewById<RadioButton>(R.id.rb_auto_send_chat)
        val tvEntryComplete = view.findViewById<View>(R.id.tv_entry_complete)

        btnBack.setOnClickListener {
            navController.navigate(R.id.action_expense_entry_to_homeFragment)
        }


        // 완료 버튼 클릭 이벤트
        tvEntryComplete.setOnClickListener {
            val detail = etDetailInput.text.toString()
            val amountString = etAmountInput.text.toString().replace(",", "")
            val amount = amountString.toIntOrNull() ?: 0 // 숫자로 변환, 기본값 0
            val date = etDateInput.text.toString()
            val category = SpendingCategory.CUSTOM(detail) // 예시로 `detail`을 사용하여 name을 설정

            if (detail.isBlank() || amount <= 0) {
                Toast.makeText(requireContext(), "지출 내용과 금액을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newHistory = BotSheetUiState.Spending.History(
                name = detail,
                price = amount
            )

            // ViewModel에 지출 데이터 추가
            botSheetViewModel.addExpenseToCategory(category, newHistory)

            Toast.makeText(requireContext(), "지출이 저장되었습니다.", Toast.LENGTH_SHORT).show()

            // 홈 화면으로 이동
            //navController.navigate(R.id.action_expense_entry_to_homeFragment)
        }


        // RadioButton 초기화 - 저장된 상태 복원
        rbNoExpense.isChecked = sharedPreferences.getBoolean("noExpenseSelected", false)
        rbAutoSendChat.isChecked = sharedPreferences.getBoolean("autoSendChat", true)

        // RadioButton 클릭 이벤트
        rbNoExpense.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("noExpenseSelected", isChecked).apply()
        }

        // rb_auto_send_chat 클릭 이벤트
        rbAutoSendChat.setOnClickListener {
            // 상태를 반전시켜 저장
            val isChecked = !rbAutoSendChat.isChecked
            rbAutoSendChat.isChecked = isChecked
            sharedPreferences.edit().putBoolean("autoSendChat", isChecked).apply()
        }

        flPhotoFrame.setOnClickListener {
            openGallery()
        }

        // et_date_input 클릭 이벤트
        etDateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val dayOfWeek = getDayOfWeek(selectedYear, selectedMonth, selectedDay)
                    etDateInput.setText("${selectedYear}년 ${selectedMonth + 1}월 ${selectedDay}일 $dayOfWeek")
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }

        // et_detail_input 초기화
        etDetailInput.setText(sharedPreferences.getString("detailInput", ""))

        // et_detail_input 텍스트 변경 리스너
        etDetailInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (it.length > 24) {
                        etDetailInput.error = "최대 24자까지 입력 가능합니다."
                    } else {
                        sharedPreferences.edit().putString("detailInput", it.toString()).apply()
                    }
                }
            }
        })

        // et_amount_input 초기화
        etAmountInput.setText(sharedPreferences.getString("amountInput", ""))

        // et_amount_input 텍스트 변경 리스너
        etAmountInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    val input = it.toString().replace(",", "") // 숫자만 추출
                    if (input.isNotEmpty()) {
                        val number = input.toLong()

                        // 99,999,999를 초과하면 최대값으로 설정
                        if (number > 99_999_999) {
                            etAmountInput.setText("99,999,999")
                            etAmountInput.setSelection(etAmountInput.text.length)
                            sharedPreferences.edit().putString("amountInput", "99,999,999").apply()
                        } else {
                            // 천 단위 콤마 추가
                            val formatted = String.format("%,d", number)
                            if (formatted != it.toString()) {
                                etAmountInput.setText(formatted)
                                etAmountInput.setSelection(etAmountInput.text.length)
                            }
                            sharedPreferences.edit().putString("amountInput", formatted).apply()
                        }
                    }
                }
            }
        })

    }
    private fun saveEntryData(entryData: EntryData) {
        with(sharedPreferences.edit()) {
            putBoolean("noExpenseSelected", entryData.noExpense)
            putBoolean("autoSendChat", entryData.autoSendChat)
            putString("dateInput", entryData.dateInput)
            putString("detailInput", entryData.detailInput)
            putString("amountInput", entryData.amountInput)
            putString("photoUri", entryData.photoUri)
            apply() // 변경 사항 저장
        }
    }

    private fun openGallery() {
        // 갤러리를 여는 Intent 생성
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK) {
            data?.data?.let { uri ->
                val imgPhotoFrame = view?.findViewById<ImageView>(R.id.img_photo_frame)
                val ivPhotoIcon = view?.findViewById<ImageView>(R.id.iv_photo_icon)

                // 선택된 이미지 URI를 ImageView에 표시
                imgPhotoFrame?.setImageURI(uri)
                // 선택된 사진 URI를 SharedPreferences에 저장
                sharedPreferences.edit().putString("photoUri", uri.toString()).apply()
                // FrameLayout의 배경 제거 (선택된 이미지만 표시)
                ivPhotoIcon?.visibility = View.GONE
            }
        }
    }

    // 요일 반환 함수
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
    private fun clearData() {
        with(sharedPreferences.edit()) {
            clear() // 모든 데이터 삭제
            apply()
        }
    }
    override fun onStop() {
        super.onStop()
        clearData() // 화면 종료 시 데이터 초기화
    }
}