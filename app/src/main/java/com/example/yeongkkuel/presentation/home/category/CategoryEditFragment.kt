package com.example.yeongkkuel.presentation.home.category

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentCategoryEditBinding

class CategoryEditFragment : Fragment() {

    private var _binding: FragmentCategoryEditBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CategoryViewModel by activityViewModels()
    private var selectedColor: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 전달받은 데이터 가져오기
        val categoryName = arguments?.getString("categoryName")
        val categoryColor = arguments?.getInt("categoryColor") ?: android.graphics.Color.BLACK

        // 데이터 화면에 표시
        binding.etCategoryEditInput.setText(categoryName)
        binding.ivSelectedColor.setBackgroundResource(R.drawable.bg_color_circle)
        binding.ivSelectedColor.background.setTint(categoryColor)
        selectedColor = categoryColor

        // 색상 팔레트 어댑터 설정
        val colorAdapter = ColorPaletteAdapter { selectedColor ->
            updateSelectedColor(selectedColor)
        }

        // RecyclerView 레이아웃 매니저 설정
        binding.rvColorPalette.layoutManager = GridLayoutManager(requireContext(), 5)
        binding.rvColorPalette.adapter = colorAdapter
        colorAdapter.submitList(getColorList()) // 색상 리스트 설정

        // 이벤트 설정
        setupTextWatcher() // 텍스트 입력 감지 추가
        setupListeners()
        updateSaveButtonState() // 초기 저장 버튼 상태 업데이트
    }

    private fun setupTextWatcher() {
        binding.etCategoryEditInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 입력된 텍스트 길이 계산
                val length = s?.length ?: 0
                binding.tvCharacterCount.text = "$length/16"

                // 저장 버튼 상태 업데이트
                updateSaveButtonState()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupListeners() {
        // 색상 변경 이벤트
        binding.ivDropdownIcon.setOnClickListener {
            toggleColorPaletteVisibility()
        }

        // 완료 버튼 클릭 이벤트
        binding.tvCategoryEdit.setOnClickListener {
            saveEditedCategory()
        }

        // 뒤로가기 버튼
        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun toggleColorPaletteVisibility() {
        val isVisible = binding.rvColorPalette.visibility == View.VISIBLE
        binding.rvColorPalette.visibility = if (isVisible) View.GONE else View.VISIBLE
        binding.cardColorPalette.visibility = binding.rvColorPalette.visibility
        binding.ivDropdownIcon.setImageResource(
            if (isVisible) R.drawable.ic_dropdown_arrow else R.drawable.ic_dropdown_arrow_up
        )
    }

    private fun updateSelectedColor(color: Int) {
        selectedColor = color
        binding.ivSelectedColor.setBackgroundResource(R.drawable.bg_color_circle)
        binding.ivSelectedColor.background.setTint(color)
        binding.rvColorPalette.visibility = View.GONE
        binding.cardColorPalette.visibility = View.GONE
        binding.ivDropdownIcon.setImageResource(R.drawable.ic_dropdown_arrow)
        updateSaveButtonState() // 저장 버튼 상태 업데이트
    }

    private fun getColorList(): List<Int> {
        return listOf(
            R.color.red1, R.color.red2, R.color.pink3, R.color.purple4, R.color.purple5,
            R.color.blue6, R.color.blue7, R.color.blue8, R.color.green9, R.color.green10,
            R.color.green11, R.color.green12, R.color.yellow13, R.color.orange14, R.color.orange15
        ).map { resources.getColor(it, null) }
    }

    private fun saveEditedCategory() {
        val updatedTitle = binding.etCategoryEditInput.text.toString()
        if (updatedTitle.isBlank() || selectedColor == null) {
            Toast.makeText(requireContext(), "제목과 색상을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 전달받은 원래 카테고리 이름 가져오기
        val originalCategoryName = arguments?.getString("categoryName") ?: return

        // ViewModel을 통해 데이터 업데이트
        viewModel.updateCategory(
            originalCategoryName,
            Category(name = updatedTitle, color = selectedColor!!)
        )
        Toast.makeText(requireContext(), "카테고리가 수정되었습니다.", Toast.LENGTH_SHORT).show()

        // 수정 후 카테고리 관리 페이지로 이동
        findNavController().popBackStack(R.id.categoryManageFragment, false)
    }

    private fun updateSaveButtonState() {
        val title = binding.etCategoryEditInput.text.toString()
        val isEnabled = title.isNotBlank() && selectedColor != null
        binding.tvCategoryEdit.isEnabled = isEnabled

        val buttonColor = if (isEnabled) R.color.button_enabled else R.color.button_disabled
        binding.tvCategoryEdit.setBackgroundColor(
            ContextCompat.getColor(requireContext(), buttonColor)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
