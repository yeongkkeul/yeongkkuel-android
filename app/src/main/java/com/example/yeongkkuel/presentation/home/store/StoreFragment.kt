package com.example.yeongkkuel.presentation.home.store

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentStoreBinding
import com.google.android.material.tabs.TabLayout

class StoreFragment : Fragment() {
    private lateinit var navController: NavController
    private var products: MutableList<Product> = mutableListOf() // MutableList 사용
    private val viewModel: StoreViewModel by viewModels()

    private var _binding: FragmentStoreBinding? = null
    private val binding get() = _binding!!

    private lateinit var storeAdapter: StoreAdapter
    private val productList = mutableListOf<Product>()

    companion object {
        // 앱 실행 동안 유지되는 MY 상품 목록
        val myProducts = mutableListOf<Product>()
    }
    private var selectedProduct: Product? = null
    private var selectedProductInMyTab: Product? = null  // 🔹 MY 탭에서 선택한 상품 저장

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
        navController = Navigation.findNavController(view)
        binding.imgGoHome.setOnClickListener {
            navController.navigate(R.id.action_storeFragment_to_homeFragment)
        }

        binding.imgPurchaseIcon.setOnClickListener {
            selectedProduct?.let {
                Log.d("StoreFragment", "Passing to Dialog: ${it.name}, ResId: ${it.imageResId}")
                showPurchaseDialog(it)
            } ?: Log.d("StoreFragment", "No Product Selected")
        }
        binding.imgSaveIcon.setOnClickListener {
            selectedProductInMyTab?.let { product ->
                val purchaseIdList = listOf(product.id)
                viewModel.saveEquippedSkins(purchaseIdList)
                Toast.makeText(requireContext(), "스킨 착용을 저장 중...", Toast.LENGTH_SHORT).show()
            } ?: Log.d("StoreFragment", "No product selected in MY tab.")
        }
        viewModel.equipResponse.observe(viewLifecycleOwner) { response ->
            if (response?.isSuccess == true) {
                Toast.makeText(requireContext(), "스킨 착용이 저장되었습니다!", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            } else {
                Toast.makeText(requireContext(), "스킨 착용 저장 실패: ${response?.message ?: "오류 발생"}", Toast.LENGTH_SHORT).show()
            }
        }

        binding.imgPurchaseIcon.setOnClickListener {
            selectedProduct?.let { product ->
                viewModel.purchaseSkin(
                    itemId = product.id,
                    itemType = product.category.name, // 예: "SWING"
                    itemName = product.name,
                    reward = product.price
                )
                Toast.makeText(requireContext(), "스킨 구매 중...", Toast.LENGTH_SHORT).show()
            } ?: Log.d("StoreFragment", "선택된 상품 없음")
        }

        // ✅ 스킨 구매 응답 처리
        viewModel.purchaseResponse.observe(viewLifecycleOwner) { response ->
            if (response?.isSuccess == true) {
                Toast.makeText(requireContext(), "스킨 구매 성공!", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            } else {
                Toast.makeText(
                    requireContext(),
                    "스킨 구매 실패: ${response?.message ?: "오류 발생"}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        viewModel.fetchShopData("SWING")
        viewModel.shopResponse.observe(viewLifecycleOwner) { response ->
            if (response?.isSuccess == true) {
                binding.tvCoin.text = "보유 리워드: ${response.result.myReward}"

                val shopItems = response.result.itemList.map { shopItem ->
                    Product(
                        id = shopItem.id,
                        name = shopItem.itemName,
                        price = shopItem.price,
                        imageResId = getDrawableFromUrl(shopItem.itemImg), // 이미지 변환
                        category = ProductCategory.valueOf(response.result.itemType)
                    )
                }

                updateProductList(shopItems)
            } else {
                Toast.makeText(requireContext(), "상점 데이터 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
        }

        setupRecyclerView()
        setupTabLayout()
    }
    private fun getDrawableFromUrl(url: String): Int {
        return when (url) {
            "swing_1.png" -> R.drawable.img_product_swing1
            "toy_1.png" -> R.drawable.img_product_toy1
            "bowl_1.png" -> R.drawable.img_product_bowl1
            "nest_1.png" -> R.drawable.img_product_nest1
            else -> R.drawable.img_product_swing1
        }
    }

    private fun setupRecyclerView() {
        storeAdapter = StoreAdapter(productList) { product ->
            if (binding.tabLayout.selectedTabPosition == 4) {  // 🔹 MY 탭에서 선택한 경우
                selectedProductInMyTab = product
            } else {
                selectedProduct = product
            }
            when (product.category) {
                ProductCategory.SWING -> {
                    val imageResId = when (product.name) {
                        "그네 1" -> R.drawable.img_home_swing1
                        "그네 2" -> R.drawable.img_home_swing2
                        else -> product.imageResId
                    }
                    updateImage(binding.imgStoreSwing, imageResId)
                    Log.d("StoreFragment", "Swing Image Updated: $imageResId")
                }
                ProductCategory.TOY -> {
                    val imageResId = when (product.name) {
                        "탱탱볼" -> R.drawable.img_home_toy1
                        "스케이트 보드" -> R.drawable.img_home_toy2
                        else -> product.imageResId
                    }
                    updateImage(binding.imgStoreToy, imageResId)
                    Log.d("StoreFragment", "Toy Image Updated: $imageResId")
                }
                ProductCategory.BOWL -> {
                    val imageResId = when (product.name) {
                        "밥그릇 1" -> R.drawable.img_home_bowl1
                        "밥그릇 2" -> R.drawable.img_home_bowl2
                        else -> product.imageResId
                    }
                    updateImage(binding.imgStoreBowl, imageResId)
                    Log.d("StoreFragment", "Bowl Image Updated: $imageResId")
                }
                ProductCategory.NEST -> {
                    val imageResId = when (product.name) {
                        "둥지 1" -> R.drawable.img_home_nest1
                        "둥지 2" -> R.drawable.img_home_nest2
                        else -> product.imageResId
                    }
                    updateImage(binding.imgStoreNest, imageResId)
                    Log.d("StoreFragment", "Nest Image Updated: $imageResId")
                }


            }

        }

        binding.rvStoreItems.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = storeAdapter
            addItemDecoration(GridSpacingItemDecoration(3, 12.toPx(requireContext()), true))
        }

        // 초기 데이터: 그네 카테고리로 필터링
        updateProductList(
            ProductUiState.init().productList.filter {
                it.category == ProductCategory.SWING
            }.map { productUiStateProduct ->
                Product(
                    id = productUiStateProduct.id,
                    name = productUiStateProduct.name,
                    price = productUiStateProduct.price,
                    imageResId = adjustResourceId(productUiStateProduct.iconResId),
                    category = productUiStateProduct.category
                )
            }
        )
    }

    private fun adjustResourceId(originalResId: Int): Int {
        return when (originalResId) {
            R.drawable.img_home_toy2 -> R.drawable.img_product_toy2
            R.drawable.img_home_toy1 -> R.drawable.img_product_toy1
            R.drawable.img_home_bowl1 -> R.drawable.img_product_bowl1
            R.drawable.img_home_bowl2 -> R.drawable.img_product_bowl2
            R.drawable.img_home_nest1 -> R.drawable.img_product_nest1
            R.drawable.img_home_nest2 -> R.drawable.img_product_nest2
            R.drawable.img_home_swing1 -> R.drawable.img_product_swing1
            R.drawable.img_home_swing2 -> R.drawable.img_product_swing2
            else -> originalResId
        }
    }

    private fun getProductsByCategory(category: ProductCategory): List<Product> {
        return ProductUiState.init().productList.filter { it.category == category }.map { productUiStateProduct ->
            Product(
                id = productUiStateProduct.id,
                name = productUiStateProduct.name,
                price = productUiStateProduct.price,
                imageResId = adjustResourceId(productUiStateProduct.iconResId),
                category = productUiStateProduct.category
            )
        }
    }

    private fun updateImage(imageView: ImageView, imageResId: Int) {
        imageView.setImageResource(imageResId)
    }

    private fun setupTabLayout() {
        val tabTitles = listOf("그네", "장난감", "밥그릇", "둥지", "MY")
        val categories = listOf(
            ProductCategory.SWING,  // 그네
            ProductCategory.TOY,    // 장난감
            ProductCategory.BOWL,   // 밥그릇
            ProductCategory.NEST,   // 둥지
            null                    // MY (null로 처리)
        )

        tabTitles.forEachIndexed { index, title ->
            val tab = binding.tabLayout.newTab()
            val isSelected = index == 0
            tab.customView = createCustomTabView(title, isSelected)
            binding.tabLayout.addTab(tab)

            val tabView = (binding.tabLayout.getChildAt(0) as ViewGroup).getChildAt(index)
            val layoutParams = tabView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.marginStart = if (index == 0) 20.toPx(requireContext()) else 12.toPx(requireContext())
            layoutParams.marginEnd = 12.toPx(requireContext())
            tabView.layoutParams = layoutParams
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // 탭 커스텀 뷰 업데이트
                tab?.customView?.let { updateTabView(it, isSelected = true) }

                // 탭 위치에 따른 동작
                tab?.position?.let { position ->
                    when (position) {
                        0 -> {
                            updateProductList(getProductsByCategory(ProductCategory.SWING))
                            showPurchaseIconOnly()
                        }
                        1 -> {
                            updateProductList(getProductsByCategory(ProductCategory.TOY))
                            showPurchaseIconOnly()
                        }
                        2 -> {
                            updateProductList(getProductsByCategory(ProductCategory.BOWL))
                            showPurchaseIconOnly()
                        }
                        3 -> {
                            updateProductList(getProductsByCategory(ProductCategory.NEST))
                            showPurchaseIconOnly()
                        }
                        4 -> {
                            updateProductList(myProducts, isMyTab = true)
                            showSaveIconOnly()
                        }
                        else -> {
                            updateProductList(emptyList())
                            showPurchaseIconOnly()
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.customView?.let { updateTabView(it, isSelected = false) }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // 재선택 이벤트 처리 필요 시 여기에 추가
            }

            // Helper 함수: Purchase 아이콘 표시
            private fun showPurchaseIconOnly() {
                binding.imgSaveIcon.visibility = View.GONE
                binding.imgPurchaseIcon.visibility = View.VISIBLE
            }

            // Helper 함수: Save 아이콘 표시
            //상품 저장
            private fun showSaveIconOnly() {
                binding.imgSaveIcon.visibility = View.VISIBLE
                binding.imgPurchaseIcon.visibility = View.GONE
            }
        })

    }

    private fun createCustomTabView(title: String, isSelected: Boolean): View {
        val tabView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_tab_view, null)
        val tabTextView = tabView.findViewById<TextView>(R.id.tabTextView)
        tabTextView.text = title
        return tabView
    }

    private fun updateTabView(view: View, isSelected: Boolean) {
        val tabTextView = view.findViewById<TextView>(R.id.tabTextView)
        tabTextView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, if (isSelected) 18f else 15f)
            setTextColor(ContextCompat.getColor(context, if (isSelected) R.color.main1 else R.color.black))
            isSingleLine = true
        }
    }

    private fun showPurchaseDialog(product: Product) {
        Log.d("showPurchaseDialog", "Product: ${product.name}, ResId: ${product.imageResId}")

        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_purchase_success, null)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val imgProduct = dialogView.findViewById<ImageView>(R.id.img_product)
        val tvProductName = dialogView.findViewById<TextView>(R.id.tv_product_name)
        val tvProductPrice = dialogView.findViewById<TextView>(R.id.tv_product_price)
        val btnConfirm = dialogView.findViewById<TextView>(R.id.tv_purchase_confirm)
        val btnCancel = dialogView.findViewById<TextView>(R.id.tv_purchase_cancel)

        imgProduct.setImageResource(product.imageResId)
        tvProductName.text = product.name

        btnConfirm.setOnClickListener {
            if (!myProducts.contains(product)) { // 중복 방지
                val updatedProduct = product.copy( // 기존 product를 그대로 사용
                    area = when (product.category) {
                        ProductCategory.SWING -> "Swing Area"
                        ProductCategory.TOY -> "Toy Area"
                        ProductCategory.BOWL -> "Bowl Area"
                        ProductCategory.NEST -> "Nest Area"
                    },
                    imageResId = product.imageResId // `mapToHomeResource`를 호출하지 않음
                )
                myProducts.add(updatedProduct)
                Log.d("MY Tab", "Product added to MY: ${updatedProduct.name}, Area: ${updatedProduct.area}")
            } else {
                Log.d("MY Tab", "Product already exists in MY: ${product.name}")
            }

            // 현재 MY 탭이 선택된 경우 UI 업데이트
            if (binding.tabLayout.selectedTabPosition == 4) {
                updateProductList(myProducts, isMyTab = true)
            }
            val bundle = Bundle().apply {
                putParcelableArrayList("myProducts", ArrayList(myProducts)) // 상품 리스트 전달
            }
            navController.navigate(R.id.action_storeFragment_to_homeFragment, bundle)

            dialog.dismiss()
        }

        // 취소 버튼 클릭 시 다이얼로그 닫기
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.window?.apply {
            setBackgroundDrawableResource(R.drawable.ic_store_topurchase) // VectorDrawable 설정
            decorView.clipToOutline = true // 💡 둥근 모서리 적용
        }

        dialog.show()
    }


    private fun updateProductList(newList: List<Product>, isMyTab: Boolean = false) {
        productList.clear()
        productList.addAll(if (isMyTab) myProducts else newList)
        storeAdapter.notifyDataSetChanged()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class GridSpacingItemDecoration(
        private val spanCount: Int,
        private val spacing: Int,
        private val includeEdge: Boolean
    ) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view)
            val column = position % spanCount

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount
                outRect.right = (column + 1) * spacing / spanCount
                if (position < spanCount) {
                    outRect.top = spacing
                }
                outRect.bottom = spacing
            } else {
                outRect.left = column * spacing / spanCount
                outRect.right = spacing - (column + 1) * spacing / spanCount
                if (position >= spanCount) {
                    outRect.top = spacing
                }
            }
        }
    }

    fun Int.toPx(context: android.content.Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

}
