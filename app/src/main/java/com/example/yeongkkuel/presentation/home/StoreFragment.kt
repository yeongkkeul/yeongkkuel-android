package com.example.yeongkkuel.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.yeongkkuel.databinding.FragmentStoreBinding
import com.google.android.material.tabs.TabLayout

class StoreFragment : Fragment() {

    private var _binding: FragmentStoreBinding? = null
    private val binding get() = _binding!!

    private lateinit var storeAdapter: StoreAdapter
    private val productList = mutableListOf<Product>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupTabLayout()
    }

    private fun setupRecyclerView() {
        storeAdapter = StoreAdapter(productList)
        binding.rvStoreItems.apply {
            layoutManager = GridLayoutManager(requireContext(), 2) // 2열 그리드 레이아웃
            adapter = storeAdapter
        }

        // 초기 데이터 로드
        updateProductList(
            ProductUiState.init().productList.filter {
                it.category.name == "장난감" // 기본 탭: 장난감
            }.map { productUiStateProduct ->
                Product(
                    name = productUiStateProduct.name,
                    price = productUiStateProduct.price,
                    imageResId = productUiStateProduct.iconResId
                )
            }
        )
    }

    private fun setupTabLayout() {
        val tabTitles = listOf("그네", "장난감", "밥그릇", "둥지", "MY")
        tabTitles.forEach { title ->
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(title))
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.text?.let { category ->
                    val filteredProducts = ProductUiState.init().productList.filter {
                        it.category.name == category
                    }.map { productUiStateProduct ->
                        Product(
                            name = productUiStateProduct.name,
                            price = productUiStateProduct.price,
                            imageResId = productUiStateProduct.iconResId
                        )
                    }
                    updateProductList(filteredProducts)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }


    private fun updateProductList(newList: List<Product>) {
        productList.clear()
        productList.addAll(newList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
