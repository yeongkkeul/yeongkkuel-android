package com.example.yeongkkuel.presentation.home.category

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private val categoryListAdapter by lazy {
        CategoryListAdapter(onCategoryClick = { category ->
            // TODO: 카테고리 클릭 시 수정/삭제 화면 이동
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
            findNavController().navigate(R.id.action_categoryManageFragment_to_categoryAddFragment)
        }

        // 뒤로가기 버튼 클릭 이벤트
        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        binding.rvCategoryList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryListAdapter

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

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoryListAdapter.submitList(categories)
            Log.d("CategoryManageFragment", "받은 데이터: $categories")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
