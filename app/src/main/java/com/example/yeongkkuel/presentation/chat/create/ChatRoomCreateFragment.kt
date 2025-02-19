package com.example.yeongkkuel.presentation.chat.create

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentChatRoomCreateBinding
import com.example.yeongkkuel.network.request.chat.ChatsRequest
import com.example.yeongkkuel.presentation.base.MainActivity
import com.example.yeongkkuel.presentation.chat.data.Age
import com.example.yeongkkuel.presentation.chat.data.Job
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import timber.log.Timber
import java.io.File

class ChatRoomCreateFragment : Fragment() {
    private lateinit var navController: NavController

    private var _binding: FragmentChatRoomCreateBinding? = null
    private val binding: FragmentChatRoomCreateBinding
        get() = requireNotNull(_binding){"FragmentChatRoomCreateBinding -> null"}

    private val viewModel: ChatRoomCreateViewModel by activityViewModels()

    private var dailyGoalExpense: Int? = null
    private var recruitmentCount: Int? = null
    private var selectedAge: Age? = null
    private var selectedJob: Job? = null

    // 선택한 이미지의 Uri를 저장
    private var selectedImageUri: Uri? = null

    // ActivityResultLauncher로 갤러리에서 이미지 선택
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // 선택한 이미지를 미리보기 ImageView에 설정 (layout에 imageViewPreview가 있다고 가정)
            binding.ivChatRoomThumbnail.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        _binding = FragmentChatRoomCreateBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).hideBottomNavigation(true)

        navController = Navigation.findNavController(view)

        binding.etChatRoomName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentLength = s?.length ?: 0
                binding.tvCountChatRoomName.text = "$currentLength/18"

                if (!s.isNullOrEmpty()) {
                    binding.tvWarningChatRoomName.visibility = View.INVISIBLE
                    binding.etChatRoomName.setBackgroundResource(R.drawable.bg_edittext_pw)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etChatRoomPwCheck.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val pw = binding.etChatRoomPw.text.toString().trim()
                val pwCheck = s?.toString()?.trim() ?: ""
                if (pwCheck.isNotEmpty() && pw == pwCheck) {
                    binding.tvWarningChatRoomPwCheck.visibility = View.INVISIBLE
                    binding.etChatRoomPwCheck.setBackgroundResource(R.drawable.bg_edittext_pw)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etChatRoomRule.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentLength = s?.length ?: 0
                binding.tvCountChatRoomRule.text = "$currentLength/200"
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        var isPasswordVisible = false
        binding.btnShowOffPw.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                binding.etChatRoomPw.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
                binding.btnShowOffPw.setImageResource(R.drawable.btn_eye_on)
            } else {
                binding.etChatRoomPw.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
                binding.btnShowOffPw.setImageResource(R.drawable.btn_eye_off)
            }
            // 커서 위치 유지
            binding.etChatRoomPw.setSelection(binding.etChatRoomPw.text.length)
        }

        binding.btnDailyGoalExpense.setOnClickListener {
            val bottomSheet = SettingDailyGoalExpenseDialogFragment { expense ->
                dailyGoalExpense = expense
                binding.btnDailyGoalExpense.text = "$expense 원"
            }
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }

        binding.btnAmountPeople.setOnClickListener {
            val bottomSheet = SettingAmountPeopleDialogFragment { people ->
                recruitmentCount = people
                binding.btnAmountPeople.text = "$people 명"
            }
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }

        binding.btnAge.setOnClickListener {
            val bottomSheet = SettingAgeDialogFragment()
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }

        viewModel.selectedAgeOption.observe(viewLifecycleOwner) { ageDisplay ->
            // ageDisplay는 "20대", "30대" 등의 문자열임
            val ageEnum = Age.entries.find { it.displayName == ageDisplay } ?: Age.UNDECIDED
            binding.btnAge.text = ageEnum.displayName
            selectedAge = ageEnum
        }

        binding.btnJob.setOnClickListener {
            val bottomSheet = SettingJobDialogFragment()
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }

        viewModel.selectedJobOption.observe(viewLifecycleOwner) { jobDisplay ->
            // jobDisplay는 "학생", "직장인" 등의 문자열임
            val jobEnum = Job.entries.find { it.displayName == jobDisplay } ?: Job.UNDECIDED
            binding.btnJob.text = jobEnum.displayName
            selectedJob = jobEnum
        }

        // 사진 버튼 클릭 시 갤러리 열기 (layout에 btnSelectImage가 있다고 가정)
        binding.clChatRoomThumbnail.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnCreate.setOnClickListener {
            createChatRoom()
        }
    }

    // 채팅방 생성 로직 (모든 UI 값 검증 후 ViewModel 호출)
    private fun createChatRoom() {
        val chatRoomName = binding.etChatRoomName.text.toString().trim()
        if (chatRoomName.isEmpty()) {
            binding.tvWarningChatRoomName.visibility = View.VISIBLE
            binding.etChatRoomName.setBackgroundResource(R.drawable.bg_edittext_pw_error)
        } else {
            binding.tvWarningChatRoomName.visibility = View.INVISIBLE
            binding.etChatRoomName.setBackgroundResource(R.drawable.bg_edittext_pw)
        }

        // 하루 목표 지출액과 모집 인원은 필수 (설정되지 않으면 버튼 텍스트 색상 변경)
        if (dailyGoalExpense == null) {
            binding.btnDailyGoalExpense.setTextColor(ContextCompat.getColor(requireContext(), R.color.error))
        }
        if (recruitmentCount == null) {
            binding.btnAmountPeople.setTextColor(ContextCompat.getColor(requireContext(), R.color.error))
        }

        // 비밀번호 검증 (비밀번호가 입력된 경우)
        val chatRoomPw = binding.etChatRoomPw.text.toString().trim()
        val chatRoomPwCheck = binding.etChatRoomPwCheck.text.toString().trim()
        if (chatRoomPw.isNotEmpty()) {
            // 비밀번호 길이가 4자가 아니면 et_chat_room_pw 배경 변경 및 에러 표시
            if (chatRoomPw.length != 4) {
                binding.etChatRoomPw.setBackgroundResource(R.drawable.bg_edittext_pw_error)
                binding.etChatRoomPw.error = "비밀번호는 4자리여야 합니다."
                return
            } else {
                binding.etChatRoomPw.setBackgroundResource(R.drawable.bg_edittext_pw)
            }
            // 비밀번호 확인이 일치하지 않으면 et_chat_room_pw_check 배경 변경 및 경고 표시
            if (chatRoomPw != chatRoomPwCheck) {
                binding.tvWarningChatRoomPwCheck.visibility = View.VISIBLE
                binding.etChatRoomPwCheck.setBackgroundResource(R.drawable.bg_edittext_pw_error)
                return
            } else {
                binding.tvWarningChatRoomPwCheck.visibility = View.INVISIBLE
                binding.etChatRoomPwCheck.setBackgroundResource(R.drawable.bg_edittext_pw)
            }
        }

        // 필수 항목 체크
        if (chatRoomName.isEmpty() || dailyGoalExpense == null || recruitmentCount == null) {
            // 필수값 누락 시 추가 처리 (예: Toast)
            Toast.makeText(requireContext(), "필수 항목을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 기타 값 추출
        val chatRoomSpendingAmountGoal = dailyGoalExpense!!
        val chatRoomMaxUserCount = recruitmentCount!!
        val chatRoomAgeRange = selectedAge ?: ""
        val chatRoomJob = selectedJob ?: ""
        val chatRoomRule = binding.etChatRoomRule.text.toString().trim() ?:""
        val chatRoomImageUrl = ""

        // ChatsRequest 생성
        val request = ChatsRequest(
            chatRoomTitle = chatRoomName,
            chatRoomPassword = chatRoomPw.ifEmpty { null },
            chatRoomSpendingAmountGoal = chatRoomSpendingAmountGoal,
            chatRoomMaxUserCount = chatRoomMaxUserCount,
            chatRoomAgeRange = chatRoomAgeRange.toString(),
            chatRoomJob = chatRoomJob.toString(),
            chatRoomRule = chatRoomRule,
            chatRoomImageUrl = chatRoomImageUrl
        )

        val imageMultipart = selectedImageUri?.let { uriToMultipart(it, "chatRoomImage") }

        // ViewModel에서 API 호출
        viewModel.postChat(request, imageMultipart) { result ->
            if (result != null) {
                // 생성 성공 시 화면 전환 등 처리
                Toast.makeText(requireContext(), "채팅방 생성 완료", Toast.LENGTH_SHORT).show()
                Timber.d("$request")
                navController.popBackStack()
            } else {
                Toast.makeText(requireContext(), "채팅방 생성 실패", Toast.LENGTH_SHORT).show()
                Timber.d("$request")
            }
        }
    }

    // Uri를 File로 변환하여 MultipartBody.Part 생성
    private fun uriToMultipart(uri: Uri, name: String): MultipartBody.Part? {
        val file = getFileFromUri(uri) ?: return null
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(name, file.name, requestBody)
    }

    // Uri로부터 임시 파일 생성
    private fun getFileFromUri(uri: Uri): File? {
        val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("upload", ".jpg", requireContext().cacheDir)
        tempFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        return tempFile
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}