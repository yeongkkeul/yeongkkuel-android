package com.example.yeongkkuel.presentation.home.entry

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentExpenseEditBinding
import com.example.yeongkkuel.presentation.botsheet.BotSheetViewModel
import com.example.yeongkkuel.presentation.home.entry.data.ExpenseUpdateRequest
import com.example.yeongkkuel.presentation.home.entry.data.ExpenseViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

class ExpenseEditFragment : Fragment() {

    private var _binding: FragmentExpenseEditBinding? = null
    private val binding get() = _binding!!

    private val botSheetViewModel: BotSheetViewModel by activityViewModels()
    private val expenseViewModel: ExpenseViewModel by activityViewModels()

    private var selectedCategoryId: Int? = null
    private var selectedDate: Date? = null
    private var selectedImageUri: Uri? = null  // 선택된 이미지 저장
    private val PICK_IMAGE_REQUEST = 1 // 갤러리 요청 코드


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpenseEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.tvEntryComplete.setOnClickListener {
            saveExpense()
        }

        binding.tvDateInput.setOnClickListener { showDatePickerDialog() }

        setupPhotoFrame()

        // 기존 지출 내역 불러오기
        val selectedExpenseId = arguments?.getInt("expenseId") ?: return
        val imageUrl = arguments?.getString("imageUrl")

        viewLifecycleOwner.lifecycleScope.launch {
            botSheetViewModel.spendingHistoryList.collectLatest { historyList ->
                val selectedExpense = historyList.find { it.id == selectedExpenseId }

                if (selectedExpense != null) {
                    binding.tvDateInput.text = formatDate(botSheetViewModel.uiState.value.date)

                    val category = getCategoryForExpense(selectedExpense.id)
                    selectedCategoryId = category?.categoryId

                    val categoryName = category?.kind?.name ?: "기타"

                    // "trash" 카테고리일 경우 UI 숨기기
                    if (categoryName.lowercase() == "trash") {
                        binding.tvExpenseCategory.visibility = View.GONE
                        binding.tvCategoryInput.visibility = View.GONE
                    } else {
                        binding.tvExpenseCategory.visibility = View.VISIBLE
                        binding.tvCategoryInput.visibility = View.VISIBLE
                        binding.tvCategoryInput.text = categoryName
                    }

                    binding.etDetailInput.setText(selectedExpense.name)
                    binding.etAmountInput.setText(selectedExpense.price.toString())

                    val updatedColor = getCategoryTextColor(categoryName)
                    binding.tvCategoryInput.setTextColor(updatedColor)

                    loadExistingImage(imageUrl)
                    enableEditing()
                }
            }
        }

        setupDetailInput()
        setupAmountInput()
    }


    private fun setupPhotoFrame() {
        binding.flPhotoFrame.setOnClickListener {
            openGallery()
        }

        binding.imgPhotoFrame.setOnClickListener {
            openGallery()
        }
    }


    // 이미지 불러오기
    private fun loadExistingImage(imageUrl: String?) {
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .into(binding.imgPhotoFrame)
            binding.ivPhotoIcon.visibility = View.GONE
        } else {
            binding.imgPhotoFrame.setImageResource(R.drawable.bg_photo_input) // 기본 이미지 설정
            binding.ivPhotoIcon.visibility = View.VISIBLE
        }
    }

    // 갤러리 열기
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // 갤러리에서 이미지 선택 후 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedImageUri = uri // 선택한 이미지 URI 저장

                // 🔹 핸드폰 화면 크기 가져오기
                val displayMetrics = requireContext().resources.displayMetrics
                val screenWidth = displayMetrics.widthPixels // 화면 너비
                val imageSize = min(screenWidth - 100, 650) // 화면보다 크지 않도록 조정 (최대 650px)

                Glide.with(this)
                    .load(uri)
                    .override(imageSize, imageSize) // 🔹 크기를 화면보다 크지 않게 설정
                    .fitCenter() // 🔹 이미지가 너무 커지지 않도록 자동 조정
                    .transform(RoundedCorners(50)) // 🔹 모서리를 둥글게 (50px)
                    .into(binding.imgPhotoFrame)

                binding.ivPhotoIcon.visibility = View.GONE // 아이콘 숨김
            }
        }
    }


    private fun enableEditing() {
        binding.etDetailInput.isEnabled = true
        binding.etAmountInput.isEnabled = true
        binding.tvCategoryInput.isEnabled = false
    }

    private fun getCategoryForExpense(expenseId: Int) = botSheetViewModel.uiState.value.spendingList.find { spending ->
        spending.history.any { it.id == expenseId }
    }

    private fun formatDate(date: Date?) = date?.let {
        SimpleDateFormat("yyyy년 M월 d일 E요일", Locale.KOREAN).format(it)
    } ?: "날짜 없음"

    private fun getCategoryTextColor(categoryName: String): Int {
        val category = botSheetViewModel.uiState.value.spendingList.find { it.kind.name == categoryName }
        return category?.color?.id?.let { ContextCompat.getColor(requireContext(), it) }
            ?: ContextCompat.getColor(requireContext(), R.color.black2)
    }

    // 지출 내용 글자 수 제한 로직 추가
    private fun setupDetailInput() {
        val etDetailInput = binding.etDetailInput
        val tvCharacterCount = binding.tvCharacterCount

        etDetailInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                tvCharacterCount.text = "$length/24" // 글자 수 표시

                if (length > 24) {
                    etDetailInput.error = "최대 24자까지 입력 가능합니다."
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // 쉼표 처리
    private fun setupAmountInput() {
        binding.etAmountInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                binding.etAmountInput.removeTextChangedListener(this)

                val rawInput = s?.toString()?.replace(",", "") ?: ""
                val input = rawInput.toLongOrNull() ?: 0

                // 🔹 최대 8자리까지만 입력 가능하도록 제한
                val trimmedInput = if (rawInput.length > 8) rawInput.substring(0, 8) else rawInput
                val limitedValue = trimmedInput.toLongOrNull()?.coerceAtMost(99_999_999) ?: 0

                // 🔹 쉼표(,)를 자동으로 추가하여 1,000,000 형식으로 표시
                val formatted = NumberFormat.getInstance(Locale.KOREAN).format(limitedValue)

                if (formatted != s.toString()) {
                    binding.etAmountInput.setText(formatted)
                    binding.etAmountInput.setSelection(formatted.length)
                }

                binding.etAmountInput.addTextChangedListener(this)
            }
        })
    }

    // 수정 API 호출
    private fun saveExpense() {
        val newDetail = binding.etDetailInput.text.toString().trim()
        val newAmount = binding.etAmountInput.text.toString().trim().replace(",", "").toIntOrNull() ?: 0

        if (newDetail.isEmpty() || newAmount <= 0) {
            Toast.makeText(requireContext(), "모든 항목을 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedExpenseId = arguments?.getInt("expenseId") ?: return
        val selectedExpense = botSheetViewModel.spendingHistoryList.value.find { it.id == selectedExpenseId } ?: return
        val formattedDate = selectedDate?.let { formatDateToApiFormat(it) }
            ?: formatDateToApiFormat(botSheetViewModel.uiState.value.date)

        val originalDate = botSheetViewModel.uiState.value.spendingList.find { spending ->
            spending.history.any { it.id == selectedExpense.id }
        }?.let { formatDateToApiFormat(botSheetViewModel.uiState.value.date) } ?: formattedDate

        // 🔹 선택된 이미지 파일을 `MultipartBody.Part`로 변환
        val imagePart = selectedImageUri?.let { uri ->
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                val tempFile = File.createTempFile("upload", ".jpg", requireContext().cacheDir)
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }

                val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("expenseImage", tempFile.name, requestFile)
            }
        }

        Log.d("ExpenseEditFragment", "✅ 기존 날짜: $originalDate, 수정된 날짜: $formattedDate")

        val updatedExpense = ExpenseUpdateRequest(
            day = formattedDate,
            categoryId = selectedCategoryId ?: return,
            content = newDetail,
            amount = newAmount
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                expenseViewModel.updateExpense(selectedExpense.id, updatedExpense, imagePart) { updateResponse -> // ✅ onComplete 추가
                    if (updateResponse?.isSuccess == true) {
                        Toast.makeText(requireContext(), "지출 내역이 수정되었습니다.", Toast.LENGTH_SHORT).show()

                        // ✅ 기존 날짜와 다른 날짜로 변경되었을 경우 처리
                        if (formattedDate != originalDate) {
                            botSheetViewModel.removeExpenseFromCategory(selectedExpense.id) // ✅ 기존 날짜에서 제거
                            botSheetViewModel.moveExpenseToNewDate(selectedExpense, formattedDate) // ✅ 새로운 날짜로 이동
                        }

                        findNavController().navigate(R.id.action_ExpenseEditFragment_to_HomeFragment)
                    } else {
                        Toast.makeText(requireContext(), "수정 실패: ${updateResponse?.message ?: "알 수 없는 오류"}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "네트워크 오류 발생. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val pickedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }.time

                selectedDate = pickedDate
                binding.tvDateInput.text = formatDate(pickedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun formatDateToApiFormat(date: Date?) = date?.let {
        SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN).format(it)
    } ?: ""

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}