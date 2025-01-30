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
        val categoryName = arguments?.getString("categoryName") ?: ""
        val categoryColorId = arguments?.getInt("categoryColor") ?: android.graphics.Color.BLACK

        // 초기 텍스트 길이 반영!
        val initialLength = categoryName.length
        binding.tvCharacterCount.text = "$initialLength/16"

        // Colors Enum 활용
        val categoryColor = Colors.fromId(categoryColorId)
        val textColor = categoryColor?.let { ContextCompat.getColor(requireContext(), it.id) }
            ?: android.graphics.Color.BLACK

        // 데이터 화면에 표시
        binding.etCategoryEditInput.setText(categoryName)
        binding.etCategoryEditInput.setTextColor(textColor)
        binding.ivSelectedColor.setBackgroundResource(R.drawable.bg_color_circle)
        binding.ivSelectedColor.background.setTint(textColor)
        selectedColor = textColor

        // 색상 팔레트 어댑터 설정
        binding.rvColorPalette.layoutManager = GridLayoutManager(requireContext(), 5)
        binding.rvColorPalette.adapter = ColorPaletteAdapter { selectedColor ->
            updateSelectedColor(selectedColor)
        }.apply {
            submitList(getColorList())
        }

        // RecyclerView, 리스너 설정
        setupRecyclerView()
        setupListeners()
        setupTextWatcher()
    }


    private fun setupRecyclerView() {
        binding.rvColorPalette.apply {
            layoutManager = GridLayoutManager(requireContext(), 5)
            adapter = ColorPaletteAdapter { selectedColor ->
                updateSelectedColor(selectedColor) // 선택한 색상 업데이트!
            }.apply {
                submitList(getColorList()) // Colors Enum에서 가져온 색상 리스트 적용
            }

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
        // Colors Enum에서 ARGB 값으로 매칭된 Enum 가져오기
        val selectedEnumColor = Colors.fromARGB(color) ?: return

        // 선택한 색상 저장
        selectedColor = color

        // View 업데이트
        binding.ivSelectedColor.setBackgroundResource(R.drawable.bg_color_circle)
        binding.ivSelectedColor.background.setTint(color)
        binding.etCategoryEditInput.setTextColor(color) // 제목 텍스트 색상 업데이트

        // 필요하다면 Enum을 기반으로 추가 작업 가능
        // 예: 로깅하거나 ViewModel에 Enum 값을 저장
        println("Selected Color Enum: $selectedEnumColor")

        binding.rvColorPalette.visibility = View.GONE
        binding.llColorPalette.visibility = View.GONE
        binding.ivDropdownIcon.setImageResource(R.drawable.ic_dropdown_arrow)
    }




    private fun saveEditedCategory() {
        val updatedTitle = binding.etCategoryEditInput.text.toString()

        // 🛠️ 제목 검증
        if (updatedTitle.isBlank()) {
            Toast.makeText(requireContext(), "제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 🎨 Colors Enum으로 변환된 색상 값 가져오기
        val selectedColorEnum = Colors.fromARGB(selectedColor ?: return) ?: run {
            Toast.makeText(requireContext(), "유효하지 않은 색상입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val originalCategoryName = arguments?.getString("categoryName") ?: return

        // 🛠️ ViewModel 업데이트
        viewModel.updateCategory(
            originalCategoryName,
            Category(name = updatedTitle, color = selectedColorEnum)
        )

        Toast.makeText(requireContext(), "카테고리가 수정되었습니다.", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack(R.id.categoryManageFragment, false)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}