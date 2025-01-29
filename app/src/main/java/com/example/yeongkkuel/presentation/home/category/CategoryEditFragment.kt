package com.example.yeongkkuel.presentation.home.category

import android.graphics.Rect
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
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentCategoryEditBinding
import com.example.yeongkkuel.presentation.util.Colors


class CategoryEditFragment : Fragment() {

    private var _binding: FragmentCategoryEditBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CategoryViewModel by activityViewModels()
    private var selectedColor: Int? = null
    private val colorPaletteAdapter by lazy {
        ColorPaletteAdapter(onColorSelected = { selectedColor ->
            updateSelectedColor(selectedColor)
        })
    }

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
        binding.etCategoryEditInput.setTextColor(categoryColor) // 제목 텍스트 색상 초기화
        selectedColor = categoryColor

        // 색상 팔레트 어댑터 설정
        val colorAdapter = ColorPaletteAdapter { selectedColor ->
            updateSelectedColor(selectedColor)
        }

        binding.rvColorPalette.layoutManager = GridLayoutManager(requireContext(), 5)
        binding.rvColorPalette.adapter = colorAdapter
        colorAdapter.submitList(getColorList())

        // 이벤트 설정
        setupTextWatcher()
        setupListeners()
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.rvColorPalette.apply {
            layoutManager = GridLayoutManager(requireContext(), 5)
            adapter = colorPaletteAdapter

            // RecyclerView 간격 설정
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    outRect.set(10, 10, 10, 10) // 간격 설정
                }
            })
        }
        colorPaletteAdapter.submitList(getColorList())
    }

    private fun getColorList(): List<Int> {
        return Colors.values().map { color ->
            ContextCompat.getColor(requireContext(), color.id) // 열거형의 id를 사용해 색상 값 가져오기
        }
    }

    private fun setupTextWatcher() {
        binding.etCategoryEditInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 입력된 텍스트 길이 계산
                val length = s?.length ?: 0
                binding.tvCharacterCount.text = "$length/16"
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
        binding.llColorPalette.visibility = binding.rvColorPalette.visibility
        binding.ivDropdownIcon.setImageResource(
            if (isVisible) R.drawable.ic_dropdown_arrow else R.drawable.ic_dropdown_arrow_up
        )
    }

    private fun updateSelectedColor(color: Int) {
        selectedColor = color
        binding.ivSelectedColor.setBackgroundResource(R.drawable.bg_color_circle)
        binding.ivSelectedColor.background.setTint(color)
        binding.etCategoryEditInput.setTextColor(color) // 제목 텍스트 색상 업데이트
        binding.rvColorPalette.visibility = View.GONE
        binding.llColorPalette.visibility = View.GONE
        binding.ivDropdownIcon.setImageResource(R.drawable.ic_dropdown_arrow)
    }


    private fun saveEditedCategory() {
        val updatedTitle = binding.etCategoryEditInput.text.toString()

        // 제목 및 색상 확인
        if (updatedTitle.isBlank()) {
            Toast.makeText(requireContext(), "제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedColor == null) {
            Toast.makeText(requireContext(), "색상을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 전달받은 원래 카테고리 이름 가져오기
        val originalCategoryName = arguments?.getString("categoryName") ?: return

        val safeSelectedColor = selectedColor // 로컬 변수로 저장
        val categoryColor = safeSelectedColor?.let { Colors.fromId(it) } ?: Colors.RED1 // Null-safe 변환
        viewModel.updateCategory(
            originalCategoryName,
            Category(name = updatedTitle, color = categoryColor)
        )
        Toast.makeText(requireContext(), "카테고리가 수정되었습니다.", Toast.LENGTH_SHORT).show()

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