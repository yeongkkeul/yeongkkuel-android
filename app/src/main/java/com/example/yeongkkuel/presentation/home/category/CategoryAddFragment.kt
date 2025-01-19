package com.example.yeongkkuel.presentation.home.category

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
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
    }

    private fun setupRecyclerView() {
        binding.rvColorPalette.apply {
            layoutManager = GridLayoutManager(requireContext(), 3) // 3열로 표시
            adapter = colorPaletteAdapter
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
        binding.ivDropdownIcon.setImageResource(
            if (isVisible) R.drawable.ic_dropdown_arrow else R.drawable.ic_dropdown_arrow_up
        )
    }

    private fun updateSelectedColor(selectedColor: Int) {
        binding.ivSelectedColor.setBackgroundColor(selectedColor)
    }

    private fun saveCategory() {
        val title = binding.etCategoryAddInput.text.toString()
        val color = (binding.ivSelectedColor.background as? ColorDrawable)?.color ?: return

        if (title.isBlank()) {
            // 제목이 비어 있으면 에러 처리
            return
        }

        viewModel.addCategory(Category(name = title, color = color))
        requireActivity().onBackPressedDispatcher.onBackPressed() // 뒤로가기
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
        ).map { requireContext().getColor(it) }
    }
}
