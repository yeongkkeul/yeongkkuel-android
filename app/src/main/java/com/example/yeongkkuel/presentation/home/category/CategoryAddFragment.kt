package com.example.yeongkkuel.presentation.home.category

import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentCategoryAddBinding
import com.example.yeongkkuel.presentation.botsheet.BotSheetViewModel
import com.example.yeongkkuel.presentation.home.category.adapter.ColorPaletteAdapter
import com.example.yeongkkuel.presentation.home.category.data.Category
import com.example.yeongkkuel.presentation.util.Colors

class CategoryAddFragment : Fragment() {

    private var _binding: FragmentCategoryAddBinding? = null
    private val binding get() = _binding!!

    // ViewModels
    private val categoryViewModel: CategoryViewModel by activityViewModels()
    private val botSheetViewModel: BotSheetViewModel by activityViewModels()

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
                    outRect.set(10, 10, 10, 10)
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

        // 키보드 완료 버튼 누르면 포커스 제거
        binding.etCategoryAddInput.setOnEditorActionListener { _, _, _ ->
            binding.etCategoryAddInput.clearFocus()
            false
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
        binding.rvColorPalette.visibility = View.GONE
        binding.llColorPalette.visibility = View.GONE
        binding.ivDropdownIcon.setImageResource(R.drawable.ic_dropdown_arrow)
        binding.etCategoryAddInput.setTextColor(color) // EditText 텍스트 색상 변경
    }

    private fun saveCategory() {
        // ✅ 저장 버튼 누를 때 EditText 포커스 제거
        binding.etCategoryAddInput.clearFocus()

        val title = binding.etCategoryAddInput.text.toString().trim()

        // 토스트 메시지 순차 실행을 위한 핸들러
        val handler = Handler(Looper.getMainLooper())

        // 제목 & 색상 입력 여부 확인
        val isTitleEmpty = title.isBlank()
        val isColorEmpty = selectedColor == null

        // 1️⃣ 제목 & 색상 모두 없을 때 (순차적으로 메시지 출력)
        if (isTitleEmpty && isColorEmpty) {
            Toast.makeText(requireContext(), "제목을 입력하세요.", Toast.LENGTH_SHORT).show()
            handler.postDelayed({
                Toast.makeText(requireContext(), "색상을 선택하세요.", Toast.LENGTH_SHORT).show()
            }, 1000) // 1초(1000ms) 후 색상 선택 토스트 띄움
            return
        }

        // 2️⃣ 제목은 있지만 색상이 없을 때
        if (!isTitleEmpty && isColorEmpty) {
            Toast.makeText(requireContext(), "색상을 선택하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 3️⃣ 제목 & 색상 입력 후 제목을 지웠을 때 (즉, 색상은 선택된 상태)
        if (isTitleEmpty && !isColorEmpty) {
            handler.post {
                Toast.makeText(requireContext(), "제목을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
            return
        }

        // ✅ 저장 로직 실행 (제목 & 색상 모두 입력됨)
        val categoryColor = Colors.entries.find { colorValue ->
            ContextCompat.getColor(requireContext(), colorValue.id) == selectedColor
        } ?: return

        val newCategory = Category(
            id = 0, // 새로운 카테고리는 서버에서 id 생성
            name = title,
            color = categoryColor
        )

        println("Category Name: ${newCategory.name}") // ✅ 제목 확인 로그 추가
        println("Category Color: ${newCategory.color}") // ✅ 색상 확인 로그 추가

        // ViewModel에 카테고리 추가
        categoryViewModel.addCategory(newCategory)
        botSheetViewModel.addCategory(newCategory)

        Toast.makeText(requireContext(), "카테고리가 저장되었습니다.", Toast.LENGTH_SHORT).show()

        // 뒤로 가기 시 CategoryAddFragment가 백스택에 남지 않도록 설정
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.categoryAddFragment, true) // categoryAddFragment를 백스택에서 제거
            .build()

        findNavController().navigate(R.id.action_categoryAddFragment_to_navigation_home, null, navOptions)
    }

    private fun setupTextWatcher() {
        binding.etCategoryAddInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 입력된 텍스트 길이 계산
                val length = s?.length ?: 0
                binding.tvCharacterCount.text = "$length/16"
            }

            override fun afterTextChanged(s: Editable?) {
                // 텍스트 입력값 저장
                val inputText = s?.toString() ?: ""

                // ✅ 제목이 있을 때만 저장 버튼 활성화, 없으면 비활성화
                binding.tvCategoryAdd.isEnabled = inputText.isNotBlank()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getColorList(): List<Int> {
        return Colors.values().map { color ->
            ContextCompat.getColor(requireContext(), color.id) // 열거형의 id를 사용해 색상 값 가져오기
        }
    }
}