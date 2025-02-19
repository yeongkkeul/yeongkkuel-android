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
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
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

    // 실제 서버에 보낼 프로필 이미지 파일
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
            val uri = result.data?.data ?: return@registerForActivityResult
            // Uri -> File 변환
            val compressedFile = UriUtil.toFile(requireContext(), uri)
            selectedFile = compressedFile

            // 미리보기 업데이트
            val bitmap = BitmapFactory.decodeFile(compressedFile.absolutePath)
            binding.ivProfile.setImageBitmap(bitmap)

            // ViewModel에 파일 경로 업데이트(필요하다면)
            viewModel.updateProfileImageUrl(compressedFile.absolutePath)
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

    /**
     * 성별/나이/직업 각 그룹에 대해 '단일 선택 + 토글 해제' 가능하도록 설정
     */
    private fun setupSelectableViews() {
        setupSingleSelection(binding.glGenderGroup) { view, isSelected ->
            // 뷰모델에 반영
            if (isSelected && view is TextView) {
                selectedGender = view
                val displayValue = view.text.toString()
                viewModel.updateGender(convertBackGender(displayValue))
            } else {
                // 해제 상태 -> UNDECIDED
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
     * 단일 선택 로직 + 이미 선택된 항목 클릭 시 선택 해제
     * onSelectionChanged: (선택된 뷰 or null, isSelected 여부)
     */
    private fun setupSingleSelection(group: ViewGroup, onSelectionChanged: (View?, Boolean) -> Unit) {
        for (i in 0 until group.childCount) {
            val child = group.getChildAt(i)
            child.setOnClickListener {
                // 이미 선택된 항목을 다시 누르면 -> 해제
                if (child.isSelected) {
                    child.isSelected = false
                    (child as? TextView)?.apply {
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                        setTextAppearance(R.style.body_regula)
                    }
                    onSelectionChanged(null, false)

                } else {
                    // 새 항목 선택하기 전에, 기존 항목들 전부 해제
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

        // 프로필 이미지 변경 버튼
        binding.tvProfileImageEdit.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }

        // 저장 버튼
        binding.tvEdit.setOnClickListener {
            // 여기서 성별/나이/직업을 꼭 선택해야 한다면 validateSelection()을 수행
            // 만약 선택 안 함을 허용하면 체크를 스킵하거나 조건을 수정
            // 예: if (!validateSelection()) return@setOnClickListener

            // ViewModel 메서드 (PATCH 등) 호출
            viewModel.saveUserProfile(selectedFile)
        }

        // 뒤로가기
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    /**
     * 만약 선택이 필수가 아니라면, 조건을 완화하거나 제거하면 됨
     */
    private fun validateSelection(): Boolean {
        val gender = (selectedGender as? TextView)?.text?.toString() ?: ""
        val ageGroup = (selectedAge as? TextView)?.text?.toString() ?: ""
        val job = (selectedJob as? TextView)?.text?.toString() ?: ""

        // 만약 "모두 필수"라면
        if (gender.isEmpty() || ageGroup.isEmpty() || job.isEmpty()) {
            Toast.makeText(requireContext(), "모든 항목을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun observeViewModel() {
        // 프로필 조회 결과
        viewModel.profileResponse.observe(viewLifecycleOwner) { response ->
            if (response.isSuccess) {
                response.result?.let { result ->
                    // UI에 기존 값 세팅
                    binding.etNickname.setText(result.nickname)
                    binding.tvNicknameCount.text = "${result.nickname.length}/10"

                    // 이미지 로딩
                    if (!result.profileImageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(result.profileImageUrl)
                            .placeholder(R.drawable.ic_my_profile)
                            .error(R.drawable.ic_my_profile)
                            .into(binding.ivProfile)
                    } else {
                        // 기본 이미지
                        binding.ivProfile.setImageResource(R.drawable.ic_my_profile)
                    }

                    // 서버 값(예: "FEMALE") -> 화면 표시값( "여자" ) 매핑
                    val displayAgeGroup = convertAgeGroup(result.ageGroup)
                    val displayGender = convertGender(result.gender)
                    val displayJob = convertJob(result.job)

                    // 기존 선택 반영
                    updateInitialSelection(binding.glGenderGroup, displayGender)
                    updateInitialSelection(binding.glAgeGroup, displayAgeGroup)
                    updateInitialSelection(binding.glJobGroup, displayJob)
                }
            } else {
                Toast.makeText(requireContext(),
                    "실패: ${response.message ?: "오류가 발생했습니다."}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // 프로필 수정(PATCH) 결과
        viewModel.updateStatus.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(requireContext(), "프로필이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }.onFailure { e ->
                Toast.makeText(requireContext(), "수정 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun updateInitialSelection(group: ViewGroup, value: String) {
        for (i in 0 until group.childCount) {
            val child = group.getChildAt(i) as? TextView
            // 매칭되는 텍스트라면 선택
            if (child?.text?.toString() == value) {
                child.isSelected = true
                child.setTextColor(ContextCompat.getColor(requireContext(), R.color.main1))
                child.setTextAppearance(R.style.body_semibo)
                when (group.id) {
                    R.id.gl_gender_group -> selectedGender = child
                    R.id.gl_age_group -> selectedAge = child
                    R.id.gl_job_group -> selectedJob = child
                }
            } else {
                // 나머지는 해제
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
            else -> "" // UNDECIDED -> "" 로 표시
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
