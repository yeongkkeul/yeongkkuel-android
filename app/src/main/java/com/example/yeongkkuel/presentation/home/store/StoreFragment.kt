package com.example.yeongkkuel.presentation.home.store

import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentStoreBinding
import com.google.android.material.tabs.TabLayout
import com.bumptech.glide.Glide


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
    private val _showFailureDialog = MutableLiveData<Boolean>()
    val showFailureDialog: LiveData<Boolean> get() = _showFailureDialog

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

        viewModel.showFailureDialog.observe(viewLifecycleOwner) { shouldShow ->
            if (shouldShow) {
                showPurchaseFailureDialog()
                viewModel.resetFailureDialog() // 다이얼로그 상태 초기화
            }
        }

        navController = Navigation.findNavController(view)
        binding.imgGoHome.setOnClickListener {
            navController.navigate(R.id.action_storeFragment_to_homeFragment)
        }

        binding.imgPurchaseIcon.setOnClickListener {
            selectedProduct?.let { product ->
                if (viewModel.currentReward < product.price) {
                    showPurchaseFailureDialog()
                } else {
                    viewModel.purchaseSkin(
                        itemId = product.id,
                        itemType = product.category.name,
                        itemName = product.name,
                        reward = product.price
                    )
                    // ✅ MY 카테고리에 추가
                    if (!myProducts.contains(product)) {
                        myProducts.add(product)
                        Log.d("StoreFragment", "✅ ${product.name}이 MY 카테고리에 추가됨")
                    }
                }
            } ?: Log.d("StoreFragment", "❌ 선택된 상품 없음")
        }

        binding.imgSaveIcon.setOnClickListener {
            selectedProductInMyTab?.let { product ->
                val bundle = Bundle().apply {
                    putParcelable("selectedProduct", product)
                }
                Log.d("StoreFragment", "✅ ${product.name} 선택됨, 홈 화면 업데이트")
                navController.navigate(R.id.action_storeFragment_to_homeFragment, bundle)
            } ?: Log.d("StoreFragment", "❌ 선택된 상품 없음")
        }

        viewModel.productUiState.observe(viewLifecycleOwner) { uiState ->
            if (uiState.productList.isNotEmpty()) {
                val productList = uiState.productList.map { productUiState ->
                    Product(
                        id = productUiState.id,
                        name = productUiState.name,
                        price = productUiState.price,
                        category = productUiState.category,
                        imageUrl = productUiState.imageUrl,
                        itemType = productUiState.itemType, // ✅ itemType 추가
                        area = mapItemTypeToArea(productUiState.itemType) // ✅ area 자동 설정
                    )
                }
                updateProductList(productList) // ✅ 변환 후 호출
            } else {
                Log.d("StoreFragment", "❌ 상품 데이터가 없습니다.")
            }
        }

        viewModel.equipResponse.observe(viewLifecycleOwner) { response ->
            if (response?.isSuccess == true) {
                Toast.makeText(requireContext(), "스킨 착용이 저장되었습니다!", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            } else {
                Toast.makeText(requireContext(), "스킨 착용 저장 실패: ${response?.message ?: "오류 발생"}", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.purchaseResponse.observe(viewLifecycleOwner) { response ->
            Log.d("StoreFragment", "🔍 스킨 구매 API 응답: $response")

            if (response?.isSuccess == true) {
                selectedProduct?.let { product ->
                    showPurchaseDialog(product) // ✅ 구매 성공 시 다이얼로그 표시
                }
            } else {
                Log.e("StoreFragment", "❌ 스킨 구매 실패: ${response?.message ?: "서버 응답 없음"}")

                // ✅ 리워드 부족 에러 감지 (예: 서버에서 리워드 부족 시 특정 코드 반환)
                if (response?.code == "REWARD_NOT_ENOUGH") {
                    showPurchaseFailureDialog()
                } else {
                    Toast.makeText(requireContext(), "스킨 구매 실패: ${response?.message ?: "서버 오류"}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        viewModel.fetchShopData("SWING")
        Log.d("StoreFragment", "🔍 Fetching shop data for category: SWING")
        viewModel.shopResponse.observe(viewLifecycleOwner) { response ->
            Log.d("StoreFragment", "🛍 API Response: $response") // ✅ 응답 확인 로그 추가

            if (response?.isSuccess == true) {
                binding.tvCoin.text = response.result.myReward.toString() // ✅ 숫자만 표시

                val shopItems = response.result.itemList.map { shopItem ->
                    Product( // ✅ ProductUiState.Product → Product 변환
                        id = shopItem.id,
                        name = shopItem.itemName,
                        price = shopItem.price ?: 0, // ✅ null 방지
                        imageUrl = shopItem.itemImg, // ✅ 서버에서 받은 이미지 URL 사용
                        category = ProductCategory.valueOf(response.result.itemType),
                        itemType = response.result.itemType, // ✅ itemType 값 추가
                        area = mapItemTypeToArea(response.result.itemType)
                    )
                }

                val selectedCategory = when (binding.tabLayout.selectedTabPosition) {
                    0 -> ProductCategory.SWING
                    1 -> ProductCategory.TOY
                    2 -> ProductCategory.BOWL
                    3 -> ProductCategory.NEST
                    else -> null
                }

                if (selectedCategory != null) {
                    val filteredItems = shopItems.filter { it.category == selectedCategory }
                    if (filteredItems.isEmpty()) {
                        Log.d("StoreFragment", "⚠ ${selectedCategory.name} 카테고리의 상품이 없습니다.")
                        updateProductList(emptyList()) // ✅ 빈 리스트 전달
                    } else {
                        updateProductList(filteredItems)
                    }
                } else {
                    updateProductList(myProducts, isMyTab = true)
                }
            } else {
                Log.e("StoreFragment", "❌ 상점 데이터 불러오기 실패: ${response?.message ?: "오류 발생"}")
            }
        }

        viewModel.fetchShopData("SWING") // ✅ 초기 데이터 로드

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
                    val imageUrl = when (product.name) {
                        "그네 1" -> "drawable/img_home_swing1" // ✅ 로컬 이미지
                        "그네 2" -> "drawable/img_home_swing2"
                        else -> product.imageUrl // ✅ 서버 이미지
                    }
                    Glide.with(binding.root.context)
                        .load(imageUrl)
                        .into(binding.imgStoreSwing)
                    Log.d("StoreFragment", "Swing Image Updated: $imageUrl")
                }

                ProductCategory.TOY -> {
                    val imageUrl = when (product.name) {
                        "탱탱볼" -> "drawable/img_home_toy1"
                        "스케이트 보드" -> "drawable/img_home_toy2"
                        else -> product.imageUrl
                    }
                    Glide.with(binding.root.context)
                        .load(imageUrl)
                        .into(binding.imgStoreToy)
                    Log.d("StoreFragment", "Toy Image Updated: $imageUrl")
                }

                ProductCategory.BOWL -> {
                    val imageUrl = when (product.name) {
                        "밥그릇 1" -> "drawable/img_home_bowl1"
                        "밥그릇 2" -> "drawable/img_home_bowl2"
                        else -> product.imageUrl
                    }
                    Glide.with(binding.root.context)
                        .load(imageUrl)
                        .into(binding.imgStoreBowl)
                    Log.d("StoreFragment", "Bowl Image Updated: $imageUrl")
                }

                ProductCategory.NEST -> {
                    val imageUrl = when (product.name) {
                        "둥지 1" -> "drawable/img_home_nest1"
                        "둥지 2" -> "drawable/img_home_nest2"
                        else -> product.imageUrl
                    }
                    Glide.with(binding.root.context)
                        .load(imageUrl)
                        .into(binding.imgStoreNest)
                    Log.d("StoreFragment", "Nest Image Updated: $imageUrl")
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
                    imageUrl = productUiStateProduct.imageUrl,
                    category = productUiStateProduct.category,
                    itemType = productUiStateProduct.itemType, // ✅ itemType 추가
                    area = mapItemTypeToArea(productUiStateProduct.itemType) // ✅ itemType을 area로 변환
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

//    private fun getProductsByCategory(category: ProductCategory): List<Product> {
//        return ProductUiState.init().productList.filter { it.category == category }.map { productUiStateProduct ->
//            Product(
//                id = productUiStateProduct.id,
//                name = productUiStateProduct.name,
//                price = productUiStateProduct.price,
//                imageUrl = productUiStateProduct.imageUrl, // ✅ imageResId 대신 imageUrl 사용
//                category = productUiStateProduct.category
//            )
//        }
//    }

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
                tab?.customView?.let { updateTabView(it, isSelected = true) }                // 탭 커스텀 뷰 업데이트
                tab?.position?.let { position ->
                    val selectedCategory = when (position) {
                        0 -> ProductCategory.SWING
                        1 -> ProductCategory.TOY
                        2 -> ProductCategory.BOWL
                        3 -> ProductCategory.NEST
                        else -> null
                    }

                    if (selectedCategory != null) {
                        Log.d("StoreFragment", "🔄 Fetching data for category: ${selectedCategory.name}") // ✅ 로그 추가
                        viewModel.fetchShopData(selectedCategory.name)// ✅ API 다시 호출
                        storeAdapter.clearSelection()
                        showPurchaseIconOnly()
                    } else {
                        updateProductList(myProducts, isMyTab = true)
                        showSaveIconOnly()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.customView?.let { updateTabView(it, isSelected = false) }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // ✅ 같은 탭을 다시 눌렀을 때도 데이터를 다시 불러오도록 함
                tab?.position?.let { position ->
                    val selectedCategory = when (position) {
                        0 -> ProductCategory.SWING
                        1 -> ProductCategory.TOY
                        2 -> ProductCategory.BOWL
                        3 -> ProductCategory.NEST
                        else -> null
                    }

                    if (selectedCategory != null) {
                        Log.d("StoreFragment", "🔄 Re-fetching data for category: ${selectedCategory.name}")
                        viewModel.fetchShopData(selectedCategory.name) // ✅ 같은 탭 다시 눌러도 데이터 로드
                        storeAdapter.clearSelection()

                    }
                }
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

        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_purchase_success, null)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // ✅ 배경을 투명하게 설정
        }
        val imgProduct = dialogView.findViewById<ImageView>(R.id.img_product)
        val tvProductName = dialogView.findViewById<TextView>(R.id.tv_product_name)
        val tvProductPrice = dialogView.findViewById<TextView>(R.id.tv_product_price)
        val btnConfirm = dialogView.findViewById<TextView>(R.id.tv_purchase_confirm)
        val btnCancel = dialogView.findViewById<TextView>(R.id.tv_purchase_cancel)

        Glide.with(imgProduct.context)
            .load(product.imageUrl) // ✅ 서버에서 받은 이미지 URL 사용
            .into(imgProduct) // ✅ 이미지뷰에 적용
        tvProductName.text = product.name
        tvProductPrice.text = product.price.toString() // ✅ 스웨거에서 받은 가격 적용

        btnConfirm.setOnClickListener {
            if (!myProducts.contains(product)) { // 중복 방지
                val updatedProduct = product.copy( // 기존 product를 그대로 사용
                    area = when (product.category) {
                        ProductCategory.SWING -> "Swing Area"
                        ProductCategory.TOY -> "Toy Area"
                        ProductCategory.BOWL -> "Bowl Area"
                        ProductCategory.NEST -> "Nest Area"
                    },
                    imageUrl = product.imageUrl // `mapToHomeResource`를 호출하지 않음
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
    private fun showPurchaseFailureDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_purchase_failure, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // ✅ 수정된 코드 (정확한 ID인지 확인)
        val btnFailureCheck = dialogView.findViewById<View>(R.id.btn_failure_check) as? TextView

        if (btnFailureCheck == null) {
            Log.e("StoreFragment", "❌ btnFailureCheck 찾을 수 없음. XML 레이아웃 ID 확인 필요!")
            return
        }

        btnFailureCheck.setOnClickListener {
            dialog.dismiss() // 다이얼로그 닫기
        }

        dialog.window?.apply {
            setBackgroundDrawableResource(R.drawable.ic_store_topurchase)
            decorView.clipToOutline = true // 둥근 모서리 적용
        }

        dialog.show()
    }
    private fun mapItemTypeToArea(itemType: String): String {
        return when (itemType) {
            "SWING" -> "Swing Area"
            "TOY" -> "Toy Area"
            "BOWL" -> "Bowl Area"
            "NEST" -> "Nest Area"
            else -> "Unknown Area" // ✅ 예외 처리
        }
    }


}