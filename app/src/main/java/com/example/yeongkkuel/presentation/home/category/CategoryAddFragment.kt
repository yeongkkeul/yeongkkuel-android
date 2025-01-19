package com.example.yeongkkuel.presentation.home.category

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentCategoryAddBinding

class CategoryAddFragment : Fragment() {

    private var _binding: FragmentCategoryAddBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CategoryViewModel by activityViewModels()
    private val colorPaletteAdapter by lazy {
        ColorPaletteAdapter(onColorSelected = { selectedColor ->
            updateSelectedColor(selectedColor)
        })
    }
    private var selectedColor: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        setupTextWatcher() // 글자 수 업데이트 로직 호출
        updateSaveButtonState() // 초기 저장 버튼 상태 업데이트
    }

    private fun setupRecyclerView() {
        binding.rvColorPalette.apply {
            layoutManager = GridLayoutManager(requireContext(), 5) // 1줄에 5개의 컬러 동그라미로 변경
            adapter = colorPaletteAdapter

            // 아이템 간 간격 설정
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    outRect.set(0, 1, 0, 1) // 좌우/상하 8dp 간격 추가
                }
            })
        }
        colorPaletteAdapter.submitList(getColorList()) // 색상 팔레트 데이터 설정
    }

    private fun setupListeners() {
        // 드롭다운 아이콘 클릭 이벤트
        binding.ivDropdownIcon.setOnClickListener {
            toggleColorPaletteVisibility()
        }

        // 저장 버튼 클릭 이벤트
        binding.tvCategoryAdd.setOnClickListener {
            saveCategory()
        }

        // 뒤로가기 버튼 클릭 이벤트
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

    private fun saveCategory() {
        val title = binding.etCategoryAddInput.text.toString()
        val color = selectedColor ?: return

        if (title.isBlank()) {
            Toast.makeText(requireContext(), "제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.addCategory(Category(name = title, color = color))
        Toast.makeText(requireContext(), "카테고리가 저장되었습니다.", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.action_categoryAddFragment_to_categoryManageFragment)
    }

    private fun setupTextWatcher() {
        binding.etCategoryAddInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateSaveButtonState() // 글자가 입력될 때마다 저장 버튼 상태 업데이트
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun updateSaveButtonState() {
        val title = binding.etCategoryAddInput.text.toString()
        val isEnabled = title.isNotBlank() && selectedColor != null
        binding.tvCategoryAdd.isEnabled = isEnabled
        val buttonColor = if (isEnabled) R.color.button_enabled else R.color.button_disabled
        binding.tvCategoryAdd.setBackgroundColor(ContextCompat.getColor(requireContext(), buttonColor))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getColorList(): List<Int> {
        return listOf(
            R.color.red1, R.color.red2, R.color.pink3, R.color.purple4, R.color.purple5,
            R.color.blue6, R.color.blue7, R.color.blue8, R.color.green9, R.color.green10,
            R.color.green11, R.color.green12, R.color.yellow13, R.color.orange14, R.color.orange15
        ).map { ContextCompat.getColor(requireContext(), it) }
    }
}
