package com.example.yeongkkuel.presentation.home.category

import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentCategoryManageBinding
import com.example.yeongkkuel.presentation.botsheet.BotSheetViewModel
import com.example.yeongkkuel.presentation.home.category.adapter.CategoryAdapter
import com.example.yeongkkuel.presentation.home.category.data.Category
import kotlinx.coroutines.launch

class CategoryManageFragment : Fragment() {

    private var _binding: FragmentCategoryManageBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CategoryViewModel by activityViewModels()
    private val botSheetViewModel: BotSheetViewModel by activityViewModels()

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // categoryAdapter 초기화
        categoryAdapter = CategoryAdapter(
            onCategoryClick = { category -> navigateToCategoryDetail(category) },
            onStartDrag = { viewHolder -> itemTouchHelper.startDrag(viewHolder) }
        )

        // ItemTouchHelper 클래스 분리해서 사용
        itemTouchHelper = ItemTouchHelper(CategoryItemTouchHelper(categoryAdapter))

        // RecyclerView 설정
        setupRecyclerView()
        itemTouchHelper.attachToRecyclerView(binding.rvCategoryList) // RecyclerView와 연결

        // 데이터 가져오기 및 UI 업데이트
        observeViewModel()
        fetchCategoriesFromServer()

        // 추가 버튼 클릭 이벤트
        binding.tvCategoryAdd.setOnClickListener {
            val categoryCount = viewModel.categories.value?.size ?: 0
            if (categoryCount >= 6) {
                showLimitReachedPopup()
            } else {
                findNavController().navigate(R.id.action_categoryManageFragment_to_categoryAddFragment)
            }
        }

        // 뒤로가기 버튼 클릭 이벤트
        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchCategories()

        Handler(Looper.getMainLooper()).postDelayed({
            viewModel.fetchCategories()
        }, 500)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            // 카테고리 데이터 관찰
            viewModel.categories.observe(viewLifecycleOwner) { categories ->
                if (categories.isNotEmpty()) {
                    categoryAdapter.submitList(categories)  // submitList()로 RecyclerView 갱신
                }
            }

            // 바텀시트 데이터 관찰
            botSheetViewModel.categoryList.observe(viewLifecycleOwner) { categories ->
                if (categories.isNotEmpty()) {
                    categoryAdapter.submitList(categories)  // submitList()로 RecyclerView 갱신
                }
            }

            // 에러 메시지 관찰
            viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
                errorMessage?.let {
                    Log.e("CategoryManageFragment", "❌ ViewModel 에러 발생: $it")
                    android.widget.Toast.makeText(requireContext(), it, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvCategoryList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter

            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                    outRect.top = 16
                    outRect.bottom = 16
                }
            })
        }
    }

    private fun fetchCategoriesFromServer() {
        viewModel.fetchCategories()
    }

    private fun navigateToCategoryDetail(category: Category) {
        val bundle = Bundle().apply {
            putString("categoryName", category.name)
            putInt("categoryColor", category.color.id)
        }
        findNavController().navigate(R.id.action_categoryManageFragment_to_categoryDetailFragment, bundle)
    }

    private fun showLimitReachedPopup() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_category_limit, null)
        val dialog = android.app.AlertDialog.Builder(requireContext()).setView(dialogView).create()

        dialogView.findViewById<TextView>(R.id.tv_reward).setOnClickListener {
            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
