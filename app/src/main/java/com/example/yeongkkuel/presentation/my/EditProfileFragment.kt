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

    // ViewModel 연결
    private val viewModel: ProfileViewModel by viewModels()

//    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

//    private var selectedImageFile: File? = null
// 실제 서버 전송용 파일
    private var selectedFile: File? = null

    // 이미지 선택 ActivityResult
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            uri?.let {
                // Uri -> File (JPEG 압축) -> selectedFile
                val compressedFile = UriUtil.toFile(requireContext(), it)
                selectedFile = compressedFile

                // 미리보기
                val bitmap = BitmapFactory.decodeFile(compressedFile.absolutePath)
                binding.ivProfile.setImageBitmap(bitmap)
                // 필요하면 ViewModel에 경로 업데이트
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

        // 갤러리에서 이미지 가져오기 위한 launcher 등록
        /*imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                uri?.let {
                    // ContentResolver를 사용하여 선택한 이미지 데이터를 임시 파일로 복사
                    val tempFile = createFileFromUri(it)
                    if (tempFile != null) {
                        selectedImageFile = tempFile  // 실제 파일 보관

                        // 파일로부터 Bitmap 생성 (캐시된 파일을 읽어오기 때문에 FileNotFound 오류 방지)
                        val bitmap = BitmapFactory.decodeFile(tempFile.absolutePath)
                        val circularBitmap = bitmap.toCircularBitmap()
                        binding.ivProfile.setImageBitmap(circularBitmap)

                        // ViewModel에 파일의 경로(또는 필요에 따라 URI)를 업데이트 (API 전송 시 사용)
                        viewModel.updateProfileImageUrl(tempFile.absolutePath)
                    } else {
                        Toast.makeText(requireContext(), "이미지 로드 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }*/
        /*imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                uri?.let {
                    val tempFile = createFileFromUri(it)
                    if (tempFile != null) {
                        selectedImageFile = tempFile  // 실제 파일 저장
                        // Bitmap 생성 및 원형 변환
                        val bitmap = BitmapFactory.decodeFile(tempFile.absolutePath)
                        val circularBitmap = bitmap.toCircularBitmap()
                        binding.ivProfile.setImageBitmap(circularBitmap)
                        // 필요 시 ViewModel에 파일 경로 업데이트 (여기서는 저장 시 파일 객체를 직접 전달)
                        viewModel.updateProfileImageUrl(tempFile.absolutePath)
                    } else {
                        Toast.makeText(requireContext(), "이미지 로드 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }*/



        // 화면 진입 시 기존 프로필 불러오기
//        viewModel.fetchUserProfile()
        setupSelectableViews()
        setupListeners()
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

        // 이미지 편집 버튼
        binding.tvProfileImageEdit.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }

        // 저장 버튼
        binding.tvEdit.setOnClickListener {
            if (validateSelection()) {
                // ViewModel 메서드 호출
                viewModel.saveUserProfile(selectedFile)
                Toast.makeText(requireContext(), "프로필이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

        // 뒤로가기
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    /*fun Bitmap.toCircularBitmap(): Bitmap {
        val size = minOf(width, height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = Rect(0, 0, size, size)
        val rectF = RectF(rect)

        canvas.drawOval(rectF, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(this, rect, rect, paint)

        return output
    }*/

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
        // 조회/수정 결과 모두 이 LiveData로 받아서 처리
        viewModel.profileResponse.observe(viewLifecycleOwner) { response ->
            // 서버 응답이 성공일 때
            if (response.isSuccess) {
                response.result?.let { result ->
                    // 조회 시: UI에 기존 값들 반영
                    binding.etNickname.setText(result.nickname)
                    result.profileImageUrl?.takeIf { it.isNotEmpty() }?.let { url ->
                        val imageUrl = result.profileImageUrl  // "https://yeongkkeul-s3.s3.ap-northeast-2.amazonaws.com/user-profile/11"
                        Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_my_profile)  // 로딩 중 표시
                            .error(R.drawable.ic_my_profile)        // 에러 시 표시
                            .into(binding.ivProfile)
                    } ?: run {
                        // 기본 이미지 설정 또는 아무 작업도 하지 않음
                        binding.ivProfile.setImageResource(R.drawable.ic_my_profile)
                    }
                    binding.tvNicknameCount.text = "${result.nickname.length}/10"

                    // 기존에 선택된 값들도 반영
                    val displayAgeGroup = convertAgeGroup(result.ageGroup)
                    val displayGenderGroup  = convertGender(result.gender)
                    val displayJobGroup = convertJob(result.job)

                    updateInitialSelection(binding.glGenderGroup,displayGenderGroup )
                    updateInitialSelection(binding.glAgeGroup, displayAgeGroup)
                    updateInitialSelection(binding.glJobGroup, displayJobGroup)
                }
            } else {
                // 에러 처리
                Toast.makeText(requireContext(),
                    "실패: ${response.message ?: "오류가 발생했습니다."}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        viewModel.updateStatus.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                // PATCH 요청이 정상적으로 완료된 시점
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
            else -> apiAgeGroup // 추가 케이스가 있으면 여기에 추가
        }
    }
    // 성별 변환
    private fun convertGender(apiGender: String): String {
        return when (apiGender.uppercase()) {
            "FEMALE" -> "여자"
            "MALE" -> "남자"
            else -> apiGender // 추가 케이스가 있으면 여기에 추가
        }
    }

    // 직업 변환
    private fun convertJob(apiJob: String): String {
        return when (apiJob.uppercase()) {
            "STUDENT" -> "학생"
            "EMPLOYEE" -> "직장인"
            "SELF_EMPLOYED" -> "자영업자"
            "HOMEMAKER" -> "주부"
            "UNDECIDED" -> ""
            else -> "" // 추가 케이스가 있으면 여기에 추가
        }
    }

    // 반대로 변환
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


    // 임시 파일 생성 함수
    private fun createFileFromUri(uri: Uri): File? {
        return try {
            // 캐시 디렉토리에 임시 파일 생성
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("profile_image", ".png", requireContext().cacheDir)
            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Bitmap을 원형으로 변환하는 확장 함수
    fun Bitmap.toCircularBitmap(): Bitmap {
        val size = minOf(width, height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = Rect(0, 0, size, size)
        val rectF = RectF(rect)
        canvas.drawOval(rectF, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(this, rect, rect, paint)
        return output
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}