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
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentEditProfileBinding
import org.w3c.dom.Text

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    // ViewModel 연결
    private val viewModel: ProfileViewModel by viewModels()

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

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
        imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                uri?.let {
                    val inputStream = requireContext().contentResolver.openInputStream(it)
                    val bitmap = BitmapFactory.decodeStream(inputStream)

                    // 원형 변환 후 이미지 적용
                    val circularBitmap = bitmap.toCircularBitmap()
                    binding.ivProfile.setImageBitmap(circularBitmap)

                    // ViewModel에 이미지 URI 저장
                    viewModel.updateProfileImageUrl(uri.toString())
                }
            }
        }

        setupSelectableViews()
        setupListeners()
        observeViewModel()

        // 화면 진입 시 기존 프로필 불러오기
        viewModel.fetchUserProfile()
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
                        viewModel.updateGender((child as TextView).text.toString())
                    }
                    R.id.gl_age_group -> {
                        selectedAge = child
                        viewModel.updateAgeGroup((child as TextView).text.toString())
                    }
                    R.id.gl_job_group -> {
                        selectedJob = child
                        viewModel.updateJob((child as TextView).text.toString())
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
                viewModel.saveUserProfile()
                Toast.makeText(requireContext(), "프로필이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

        // 뒤로가기
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

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
                    binding.ivProfile.setImageURI(Uri.parse(result.profileImageUrl))
                    binding.tvNicknameCount.text = "${result.nickname.length}/10"

                    // 기존에 선택된 값들도 반영
                    updateInitialSelection(binding.glGenderGroup, result.gender)
                    updateInitialSelection(binding.glAgeGroup, result.ageGroup)
                    updateInitialSelection(binding.glJobGroup, result.job)
                }
            } else {
                // 에러 처리
                Toast.makeText(requireContext(),
                    "실패: ${response.message ?: "오류가 발생했습니다."}",
                    Toast.LENGTH_SHORT
                ).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}