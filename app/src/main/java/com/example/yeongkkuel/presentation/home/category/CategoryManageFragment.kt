package com.example.yeongkkuel.presentation.home.category

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentCategoryManageBinding

class CategoryManageFragment : Fragment() {

    private var _binding: FragmentCategoryManageBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CategoryViewModel by activityViewModels()
    private val categoryAdapter by lazy {
        CategoryAdapter(onCategoryClick = { category ->
            navigateToCategoryDetail(category) // 클릭 시 상세 페이지로 이동
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryManageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView 설정
        setupRecyclerView()

        // ViewModel 관찰
        observeViewModel()

        // 추가 버튼 클릭 이벤트 - 카테고리 추가 프래그먼트로 이동
        binding.tvCategoryAdd.setOnClickListener {
            val categoryCount = viewModel.categories.value?.size ?: 0 // 카테고리 개수 가져오기

            if (categoryCount >= 6) {
                // 카테고리가 6개 이상일 때 팝업 띄우기
                showLimitReachedPopup()
            } else {
                // 카테고리가 6개 미만일 때 카테고리 추가 화면으로 이동
                findNavController().navigate(R.id.action_categoryManageFragment_to_categoryAddFragment)
            }
        }

        // 뒤로가기 버튼 클릭 이벤트
        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun showLimitReachedPopup() {
        // dialog_category_limit.xml 레이아웃을 inflate
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_category_limit, null)

        // AlertDialog에 커스텀 뷰 설정
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // 팝업의 확인 버튼 클릭 이벤트 처리
        dialogView.findViewById<TextView>(R.id.tv_reward).setOnClickListener {
            dialog.dismiss() // 팝업 닫기
        }

        // 팝업 크기와 스타일 설정
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // 배경 투명 처리
        dialog.show()
    }


    private fun setupRecyclerView() {
        binding.rvCategoryList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter

            // 카테고리 아이템 간격 추가
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    outRect.top = 16 // 각 아이템의 위쪽에 16dp 간격
                    outRect.bottom = 16 // 각 아이템의 아래쪽에 16dp 간격
                }
            })
        }
    }

    private fun navigateToCategoryDetail(category: Category) {
        val bundle = Bundle().apply {
            putString("categoryName", category.name)
            putInt("categoryColor", category.color.id) // Colors의 id를 Int로 전달
        }
        findNavController().navigate(R.id.action_categoryManageFragment_to_categoryDetailFragment, bundle)
    }

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.submitList(categories) // RecyclerView에 새로운 데이터 반영
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
