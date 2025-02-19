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

    // 1) ViewModel: Activity 범위로 공유한다고 하셨으니 이렇게 지정
    private val viewModel: ProfileViewModel by viewModels({ requireActivity() })

    // 실제 서버 전송용 파일

    private var selectedFile: File? = null

    // 이미지 선택 ActivityResult (갤러리에서 사진 선택)
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

    private var selectedGender: View? = null
    private var selectedAge: View? = null
    private var selectedJob: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // (1) 라디오 버튼(단일 선택) 세팅
        setupSelectableViews()

        // (2) 이벤트 리스너
        setupListeners()

        // (3) LiveData 관찰
        observeViewModel()
    }

    private fun setupSelectableViews() {
        val genderGroup = binding.glGenderGroup
        val ageGroup = binding.glAgeGroup
        val jobGroup = binding.glJobGroup

        setupSingleSelection(genderGroup)
        setupSingleSelection(ageGroup)
        setupSingleSelection(jobGroup)
    }

    private fun setupSingleSelection(group: ViewGroup) {
        for (i in 0 until group.childCount) {
            val child = group.getChildAt(i)
            child.setOnClickListener {
                // 기존 선택 해제
                for (j in 0 until group.childCount) {
                    val sibling = group.getChildAt(j)
                    sibling.isSelected = false
                    (sibling as? TextView)?.apply {
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                        setTextAppearance(R.style.body_regula)
                    }
                }

                // 새로 선택
                child.isSelected = true
                (child as? TextView)?.apply {
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.main1))
                    setTextAppearance(R.style.body_semibo)
                }

                // ViewModel에 반영
                when (group.id) {
                    R.id.gl_gender_group -> {
                        selectedGender = child
                        val displayValue = (child as TextView).text.toString()
                        viewModel.updateGender(convertBackGender(displayValue))
                    }
                    R.id.gl_age_group -> {
                        selectedAge = child
                        val displayValue = (child as TextView).text.toString()
                        viewModel.updateAgeGroup(convertBackAgeGroup(displayValue))
                    }
                    R.id.gl_job_group -> {
                        selectedJob = child
                        val displayValue = (child as TextView).text.toString()
                        viewModel.updateJob(convertBackJob(displayValue))
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        // 닉네임 입력
        binding.etNickname.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateNickname(s.toString())
                binding.tvNicknameCount.text = "${s?.length ?: 0}/10"
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 이미지 선택 버튼
        binding.tvProfileImageEdit.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }

        // 저장 버튼 클릭
        binding.tvEdit.setOnClickListener {
            if (validateSelection()) {

                viewModel.saveUserProfile(requireContext(),selectedFile)
            }
        }

        // 뒤로가기
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun validateSelection(): Boolean {
        val gender = (selectedGender as? TextView)?.text?.toString()
        val ageGroup = (selectedAge as? TextView)?.text?.toString()
        val job = (selectedJob as? TextView)?.text?.toString()

        if (gender.isNullOrEmpty() || ageGroup.isNullOrEmpty() || job.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "모든 항목을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun observeViewModel() {

        // (A) 기존 profileResponse 관찰 - 프로필 조회 / 수정 직후 결과
        viewModel.profileResponse.observe(viewLifecycleOwner) { response ->
            if (response.isSuccess) {
                response.result?.let { result ->

                    // 1) 닉네임 세팅
                    binding.etNickname.setText(result.nickname)
                    binding.tvNicknameCount.text = "${result.nickname.length}/10"

                    // 2) 이미지 로드: 캐시 무효화를 위해 diskCacheStrategy, skipMemoryCache 설정
                    val imageUrl = result.profileImageUrl
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .diskCacheStrategy(DiskCacheStrategy.NONE) // 캐시 끔
                            .skipMemoryCache(true)
                            .placeholder(null)
                            .error(R.drawable.bg_box_white)
                            .circleCrop()
                            .into(binding.ivProfile)
                    } else {
                        binding.ivProfile.setImageResource(R.drawable.ic_my_profile)
                    }

                    // 3) 기존에 선택된 값들
                    val displayAgeGroup = convertAgeGroup(result.ageGroup)
                    val displayGenderGroup = convertGender(result.gender)
                    val displayJobGroup = convertJob(result.job)

                    updateInitialSelection(binding.glGenderGroup, displayGenderGroup)
                    updateInitialSelection(binding.glAgeGroup, displayAgeGroup)
                    updateInitialSelection(binding.glJobGroup, displayJobGroup)
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

    // --------------------------------------------------------------------------------
    // 이하 변환 함수(기존 로직 유지)
    // --------------------------------------------------------------------------------

    private fun updateInitialSelection(group: ViewGroup, value: String) {
        for (i in 0 until group.childCount) {
            val child = group.getChildAt(i) as? TextView
            if (child?.text.toString() == value) {
                child?.isSelected = true
                child?.setTextColor(ContextCompat.getColor(requireContext(), R.color.main1))
                child?.setTextAppearance(R.style.body_semibo)
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

    private fun convertAgeGroup(apiAgeGroup: String): String {
        return when (apiAgeGroup.uppercase()) {
            "TEENAGER" -> "14~19세"
            "TWENTIES" -> "20대"
            "THIRTIES" -> "30대"
            "FORTIES" -> "40대"
            "FIFTIES" -> "50대"
            "SIXTIES_AND_ABOVE" -> "60대 이상"
            else -> apiAgeGroup
        }
    }

    private fun convertGender(apiGender: String): String {
        return when (apiGender.uppercase()) {
            "FEMALE" -> "여자"
            "MALE" -> "남자"
            else -> apiGender
        }
    }

    private fun convertJob(apiJob: String): String {
        return when (apiJob.uppercase()) {
            "STUDENT" -> "학생"
            "EMPLOYEE" -> "직장인"
            "SELF_EMPLOYED" -> "자영업자"
            "HOMEMAKER" -> "주부"
            "UNDECIDED" -> "무직"
            else -> apiJob
        }
    }

    private fun convertBackAgeGroup(displayAgeGroup: String): String {
        return when (displayAgeGroup) {
            "14~19세" -> "TEENAGER"
            "20대" -> "TWENTIES"
            "30대" -> "THIRTIES"
            "40대" -> "FORTIES"
            "50대" -> "FIFTIES"
            "60대 이상" -> "SIXTIES_AND_ABOVE"
            else -> displayAgeGroup
        }
    }

    private fun convertBackGender(displayGender: String): String {
        return when (displayGender) {
            "여자" -> "Female"
            "남자" -> "Male"
            else -> displayGender
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
