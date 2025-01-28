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
        setupTextWatcher()
    }

    private fun setupRecyclerView() {
        binding.rvColorPalette.apply {
            layoutManager = GridLayoutManager(requireContext(), 5)
            adapter = colorPaletteAdapter

            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    outRect.set(0, 1, 0, 1)
                }
            })
        }
        colorPaletteAdapter.submitList(getColorList())
    }

    private fun setupListeners() {
        binding.ivDropdownIcon.setOnClickListener {
            toggleColorPaletteVisibility()
        }

        binding.tvCategoryAdd.setOnClickListener {
            saveCategory()
        }

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

        // 색상 팔레트 UI 업데이트
        binding.ivSelectedColor.setBackgroundResource(R.drawable.bg_color_circle)
        binding.ivSelectedColor.background.setTint(color)
        binding.rvColorPalette.visibility = View.GONE
        binding.cardColorPalette.visibility = View.GONE
        binding.ivDropdownIcon.setImageResource(R.drawable.ic_dropdown_arrow)

        // 제목 EditText의 텍스트 색상 업데이트
        binding.etCategoryAddInput.setTextColor(color)
    }


    private fun saveCategory() {
        val title = binding.etCategoryAddInput.text.toString()

        // 제목 검증
        if (title.isBlank()) {
            Toast.makeText(requireContext(), "제목을 입력하지 않았습니다", Toast.LENGTH_SHORT).show()
            return
        }

        // 색상 검증
        if (selectedColor == null) {
            Toast.makeText(requireContext(), "색상을 선택하지 않았습니다", Toast.LENGTH_SHORT).show()
            return
        }

        // ViewModel에 저장
        viewModel.addCategory(Category(name = title, color = selectedColor!!))
        Toast.makeText(requireContext(), "카테고리가 저장되었습니다.", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.action_categoryAddFragment_to_categoryManageFragment)
    }

    private fun setupTextWatcher() {
        binding.etCategoryAddInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                binding.tvCharacterCount.text = "$length/16"
            }

            override fun afterTextChanged(s: Editable?) {}
        })
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
