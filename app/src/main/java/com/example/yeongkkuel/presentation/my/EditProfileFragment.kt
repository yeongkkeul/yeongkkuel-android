package com.example.yeongkkuel.presentation.my

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentEditProfileBinding
import com.example.yeongkkuel.presentation.my.util.UriUtil
import org.w3c.dom.Text
import java.io.File

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val viewModel: ProfileViewModel by viewModels()

//    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

//    private var selectedImageFile: File? = null
// 실제 서버 전송용 파일
    private var selectedFile: File? = null

    // 선택된 뷰 (없으면 null)
    private var selectedGender: View? = null
    private var selectedAge: View? = null
    private var selectedJob: View? = null

    // 이미지 픽커 런처
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            uri?.let {
                // Uri -> File
                val compressedFile = UriUtil.toFile(requireContext(), it)
                selectedFile = compressedFile

                // 미리보기  - imagepicker로 고른 이미지 미리보기로 보역주
                Glide.with(this)
                    .load(compressedFile)  // File 객체도 load 가능
                    .circleCrop()          // 원형 크롭
                    .into(binding.ivProfile)
                // 기존 로직대로, ViewModel에도 파일 경로 업데이트 (원하면 추가)
                viewModel.updateProfileImageUrl(compressedFile.absolutePath)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSelectableViews()
        setupListeners()
        observeViewModel()


    }

    private fun setupSelectableViews() {
        setupSingleSelection(binding.glGenderGroup) { view, isSelected ->
            if (isSelected && view is TextView) {
                selectedGender = view
                val displayValue = view.text.toString()
                viewModel.updateGender(convertBackGender(displayValue))
            } else {
                // 선택 해제 -> UNDECIDED
                selectedGender = null
                viewModel.updateGender("UNDECIDED")
            }
        }

        setupSingleSelection(binding.glAgeGroup) { view, isSelected ->
            if (isSelected && view is TextView) {
                selectedAge = view
                val displayValue = view.text.toString()
                viewModel.updateAgeGroup(convertBackAgeGroup(displayValue))
            } else {
                selectedAge = null
                viewModel.updateAgeGroup("UNDECIDED")
            }
        }

        setupSingleSelection(binding.glJobGroup) { view, isSelected ->
            if (isSelected && view is TextView) {
                selectedJob = view
                val displayValue = view.text.toString()
                viewModel.updateJob(convertBackJob(displayValue))
            } else {
                selectedJob = null
                viewModel.updateJob("UNDECIDED")
            }
        }
    }

    /**
     * 단일 선택 로직 + 이미 선택된 항목 클릭 시 '해제' 처리
     */
    private fun setupSingleSelection(group: ViewGroup, onSelectionChanged: (View?, Boolean) -> Unit) {
        for (i in 0 until group.childCount) {
            val child = group.getChildAt(i)
            child.setOnClickListener {
                // 이미 선택되어 있다면 -> 해제
                if (child.isSelected) {
                    child.isSelected = false
                    (child as? TextView)?.apply {
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                        setTextAppearance(R.style.body_regula)
                    }
                    onSelectionChanged(null, false)

                } else {
                    // 새 항목 선택하기 전에, 기존 항목들 해제
                    for (j in 0 until group.childCount) {
                        val sibling = group.getChildAt(j)
                        sibling.isSelected = false
                        (sibling as? TextView)?.apply {
                            setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                            setTextAppearance(R.style.body_regula)
                        }
                    }
                    // 현재 child 선택
                    child.isSelected = true
                    (child as? TextView)?.apply {
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.main1))
                        setTextAppearance(R.style.body_semibo)
                    }
                    onSelectionChanged(child, true)
                }
            }
        }
    }

    private fun setupListeners() {
        // 닉네임 입력 감지
        binding.etNickname.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val nickname = s.toString()
                viewModel.updateNickname(nickname)
                binding.tvNicknameCount.text = "${nickname.length}/10"
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 이미지 편집 버튼
        binding.tvProfileImageEdit.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }

        // 저장 버튼
        binding.tvEdit.setOnClickListener {
            // 1) 닉네임이 비었는지 검사
            if (!validateNickname()) return@setOnClickListener

            // 2) 필요하다면 '모두 필수 선택' 체크 or 스킵
            // if (!validateSelection()) return@setOnClickListener

            // ViewModel 메서드 호출 (PATCH)
//            viewModel.saveUserProfile(selectedFile)

                viewModel.saveUserProfile(requireContext(),selectedFile)

        }

        // 뒤로가기
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    /**
     * 닉네임이 비어있는지 검사 (필수)
     */
    private fun validateNickname(): Boolean {
        val nickname = binding.etNickname.text.toString().trim()
        return if (nickname.isEmpty()) {
            Toast.makeText(requireContext(), "닉네임을 입력해 주세요.", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    /**
     * 만약 성별/나이/직업도 필수라면 별도 메서드로 검사
     * 여기서는 '선택 안 해도 OK'라고 가정하여 생략. 필요 시 아래 로직 이용
     */
    private fun validateSelection(): Boolean {
        val gender = (selectedGender as? TextView)?.text?.toString() ?: ""
        val ageGroup = (selectedAge as? TextView)?.text?.toString() ?: ""
        val job = (selectedJob as? TextView)?.text?.toString() ?: ""

        if (gender.isEmpty() || ageGroup.isEmpty() || job.isEmpty()) {
            Toast.makeText(requireContext(), "모든 항목을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun observeViewModel() {

        // (A) 기존 profileResponse 관찰 - 프로필 조회 / 수정 직후 결과
        // 프로필 조회 결과
        viewModel.profileResponse.observe(viewLifecycleOwner) { response ->
            if (response.isSuccess) {
                response.result?.let { result ->
                    // 조회 시: UI에 기존 값들 반영
                    binding.etNickname.setText(result.nickname)
                    binding.tvNicknameCount.text = "${result.nickname.length}/10"

                    // 프로필 이미지
                    if (!result.profileImageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(result.profileImageUrl)
                            .diskCacheStrategy(DiskCacheStrategy.NONE) // 캐시 끔
                            .skipMemoryCache(true)
                            .placeholder(null)
                            .error(R.drawable.bg_box_white)
                            .circleCrop()
                            .into(binding.ivProfile)
                    } else {
                        binding.ivProfile.setImageResource(R.drawable.ic_my_profile)
                    }

                    // 서버에서 받은 성별/나이/직업 -> 화면 표기용으로 변환
                    val displayAgeGroup = convertAgeGroup(result.ageGroup)
                    val displayGender = convertGender(result.gender)
                    val displayJob = convertJob(result.job)

                    // 기존 선택 반영
                    updateInitialSelection(binding.glGenderGroup, displayGender)
                    updateInitialSelection(binding.glAgeGroup, displayAgeGroup)
                    updateInitialSelection(binding.glJobGroup, displayJob)
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "실패: ${response.message ?: "오류가 발생했습니다."}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // (B) 이벤트 관찰 - 수정 성공/실패
        viewModel.updateStatusEvent.observe(viewLifecycleOwner) { event ->
            // 이벤트가 이미 처리되었는지 확인
            event.getContentIfNotHandled()?.let { isSuccess ->
                if (isSuccess) {
                    // 성공
                    Toast.makeText(requireContext(), "프로필이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    // 실패
                    Toast.makeText(requireContext(), "프로필 수정 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
        // 프로필 수정(PATCH) 결과
        /*viewModel.updateStatus.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(requireContext(), "프로필이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }.onFailure { e ->
                Toast.makeText(requireContext(), "수정 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }*/


    // --------------------------------------------------------------------------------
    // 이하 변환 함수(기존 로직 유지)
    // --------------------------------------------------------------------------------

    /**
     * 조회된 값에 따라 초기 선택 상태를 업데이트
     */
    private fun updateInitialSelection(group: ViewGroup, value: String) {
        for (i in 0 until group.childCount) {
            val child = group.getChildAt(i) as? TextView
            if (child?.text?.toString() == value) {
                child.isSelected = true
                child.setTextColor(ContextCompat.getColor(requireContext(), R.color.main1))
                child.setTextAppearance(R.style.body_semibo)
                // 기록
                when (group.id) {
                    R.id.gl_gender_group -> selectedGender = child
                    R.id.gl_age_group -> selectedAge = child
                    R.id.gl_job_group -> selectedJob = child
                }
            } else {
                child?.isSelected = false
                child?.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                child?.setTextAppearance(R.style.body_regula)
            }
        }
    }

    // 서버 -> 화면 변환
    private fun convertAgeGroup(apiAgeGroup: String): String {
        return when (apiAgeGroup.uppercase()) {
            "TEENAGER" -> "14~19세"
            "TWENTIES" -> "20대"
            "THIRTIES" -> "30대"
            "FORTIES" -> "40대"
            "FIFTIES" -> "50대"
            "SIXTIES_AND_ABOVE" -> "60대 이상"
            else -> ""
        }
    }
    private fun convertGender(apiGender: String): String {
        return when (apiGender.uppercase()) {
            "FEMALE" -> "여자"
            "MALE" -> "남자"
            else -> "" // UNDECIDED -> ""
        }
    }
    private fun convertJob(apiJob: String): String {
        return when (apiJob.uppercase()) {
            "STUDENT" -> "학생"
            "EMPLOYEE" -> "직장인"
            "SELF_EMPLOYED" -> "자영업자"
            "HOMEMAKER" -> "주부"
            else -> "" // UNDECIDED 혹은 기타
        }
    }

    // 화면 -> 서버 변환
    private fun convertBackAgeGroup(displayAgeGroup: String): String {
        return when (displayAgeGroup) {
            "14~19세" -> "TEENAGER"
            "20대" -> "TWENTIES"
            "30대" -> "THIRTIES"
            "40대" -> "FORTIES"
            "50대" -> "FIFTIES"
            "60대 이상" -> "SIXTIES_AND_ABOVE"
            else -> "UNDECIDED"
        }
    }
    private fun convertBackGender(displayGender: String): String {
        return when (displayGender) {
            "여자" -> "Female"
            "남자" -> "Male"
            else -> "UNDECIDED"
        }
    }
    private fun convertBackJob(displayJob: String): String {
        return when (displayJob) {
            "학생" -> "STUDENT"
            "직장인" -> "EMPLOYEE"
            "자영업자" -> "SELF_EMPLOYED"
            "주부" -> "HOMEMAKER"
            else -> "UNDECIDED"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

