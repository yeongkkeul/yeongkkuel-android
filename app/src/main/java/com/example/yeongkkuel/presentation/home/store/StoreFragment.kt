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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentStoreBinding
import com.example.yeongkkuel.presentation.home.HomeRepository
import com.example.yeongkkuel.presentation.home.HomeViewModel
import com.example.yeongkkuel.presentation.home.MySkin
import com.google.android.material.tabs.TabLayout


class StoreFragment : Fragment() {
    private lateinit var navController: NavController
    private var products: MutableList<Product> = mutableListOf() // MutableList 사용
    private val viewModel: StoreViewModel by viewModels()

    private var _binding: FragmentStoreBinding? = null
    private val binding get() = _binding!!

    private lateinit var storeAdapter: StoreAdapter
    private val productList = mutableListOf<Product>()
    private val homeViewModel: HomeViewModel by activityViewModels {
        HomeViewModel.Factory(HomeRepository())
    }

    companion object {
        // 앱 실행 동안 유지되는 MY 상품 목록
        val myProducts = mutableListOf<Product>()
    }

    private var selectedProduct: Product? = null
    private var selectedProductInMyTab: Product? = null  //  MY 탭에서 선택한 상품 저장
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
        binding.includeTopbar.imgGoHome.setOnClickListener {
            navController.navigate(R.id.action_storeFragment_to_homeFragment)
        }

        binding.imgPurchaseIcon.setOnClickListener {
            selectedProduct?.let { product ->
                if (viewModel.shopResponse.value?.result?.myReward ?: 0 < product.price) {
                    showPurchaseFailureDialog()
                } else {
                    showPurchaseDialog(product) // 다이얼로그 먼저 띄우기

                    viewModel.purchaseSkin(
                        itemId = product.id,
                        itemType = product.category.name,
                        itemName = product.name,
                        reward = product.price,
                        homeViewModel = homeViewModel // homeViewModel을 전달하도록 수정

                    )
                    //  MY 카테고리에 추가
                    if (!myProducts.contains(product)) {
                        myProducts.add(product)
                        Log.d("StoreFragment", "${product.name}이 MY 카테고리에 추가됨")
                    }
                }
            } ?: Log.d("StoreFragment", "선택된 상품 없음")
        }

        binding.imgSaveIcon.setOnClickListener {
            selectedProductInMyTab?.let { product ->
                Log.d("StoreFragment", "${product.name} 선택됨, 홈 화면 업데이트 시작")

                // 스킨 착용 API 호출
                homeViewModel.saveEquippedSkins(listOf(product.id))

                // MY 데이터 갱신 (변경된 정보 반영)
                viewModel.fetchShopData("MY")

                // UI 반영
                Toast.makeText(requireContext(), "${product.name}이 착용되었습니다!", Toast.LENGTH_SHORT).show()
                Log.d("StoreFragment", "${product.name} 착용 완료")
            } ?: Log.d("StoreFragment", " 선택된 상품 없음")
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
                        itemType = productUiState.itemType ?: "UNKNOWN" // itemType 추가
                    )
                }
                updateProductList(productList, isMyTab = true) // 변환된 productList 전달
            } else {
                Log.d("StoreFragment", "상품 데이터가 없습니다.")
            }
        }

        viewModel.equipResponse.observe(viewLifecycleOwner) { response ->
            if (response?.isSuccess == true) {
                Toast.makeText(requireContext(), "스킨 착용이 저장되었습니다!", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            } else {
                Toast.makeText(
                    requireContext(),
                    "스킨 착용 저장 실패: ${response?.message ?: "오류 발생"}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        viewModel.purchaseResponse.observe(viewLifecycleOwner) { response ->
            Log.d("StoreFragment", "스킨 구매 API 응답: $response")

            if (response?.isSuccess == true) {
                selectedProduct?.let { product ->
                    saveProductToMyTab(product) // MY 탭에 추가하는 별도 함수 호출

                    // 전체 상품을 다시 불러오지 않고, MY 데이터만 갱신
                    viewModel.fetchShopData("MY")
                }
            } else {
                Log.e("StoreFragment", "스킨 구매 실패: ${response?.message ?: "서버 응답 없음"}")

                if (response?.code == "REWARD_NOT_ENOUGH") {
                    showPurchaseFailureDialog()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "스킨 구매 실패: ${response?.message ?: "서버 오류"}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


        viewModel.fetchShopData("SWING")
        Log.d("StoreFragment", "Fetching shop data for category: SWING")
        viewModel.shopResponse.observe(viewLifecycleOwner) { response ->
            Log.d("StoreFragment", " API Response: $response") // 응답 확인 로그 추가

            if (response?.isSuccess == true) {
                binding.tvCoin.text = response.result.myReward.toString() // 숫자만 표시

                val shopItems = response.result.itemList.map { shopItem ->
                    val fixedItemType =
                        shopItem.itemType ?: response.result.itemType ?: "UNKNOWN" // 기본값 추가
                    Log.d(
                        "StoreFragment",
                        "itemType 확인 - id: ${shopItem.id}, name: ${shopItem.itemName}, itemType: ${shopItem.itemType}, fixedItemType: $fixedItemType"
                    )

//                    if (shopItem.itemType == null) {
//                        Log.e("StoreViewModel", "itemType이 null이므로 result.itemType(${response.result.itemType}) 사용")
//                    }
                    val category = when (fixedItemType) { // ProductCategory 변환
                        "SWING" -> ProductCategory.SWING
                        "TOY" -> ProductCategory.TOY
                        "BOWL" -> ProductCategory.BOWL
                        "NEST" -> ProductCategory.NEST
                        else -> ProductCategory.SWING // 예외 방지
                    }

                    Product(
                        id = shopItem.id,
                        name = shopItem.itemName,
                        price = shopItem.price ?: 0, // null 방지
                        imageUrl = shopItem.itemImg, //서버에서 받은 이미지 URL 사용
                        category = category, // 변환된 category 사용
                        itemType = fixedItemType // 수정된 코드 (클래스를 참조하지 않고 객체 참조)
                    )
                }

                val selectedCategory = when (binding.tabLayout.selectedTabPosition) {
                    0 -> ProductCategory.SWING
                    1 -> ProductCategory.TOY
                    2 -> ProductCategory.BOWL
                    3 -> ProductCategory.NEST
                    4 -> ProductCategory.MY
                    else -> null
                }

                if (selectedCategory != null) {
                    val filteredItems = shopItems.filter { it.category == selectedCategory }
                    if (filteredItems.isEmpty()) {
                        Log.d("StoreFragment", "${selectedCategory.name} 카테고리의 상품이 없습니다.")
                        updateProductList(emptyList()) // 빈 리스트 전달
                    } else {
                        updateProductList(filteredItems)
                    }
                } else {
                    Log.d("StoreFragment", "MY 탭 선택됨 - 상품 리스트 업데이트")
                    myProducts.forEach {
                        Log.d(
                            "StoreFragment",
                            "MY 탭 상품 - id: ${it.id}, name: ${it.name}, itemType: ${it.itemType}, imageUrl: ${it.imageUrl}"
                        )
                    }
                    updateProductList(myProducts, isMyTab = true)
                }
            }
        }

        viewModel.fetchShopData("SWING")
        setupRecyclerView()
        setupTabLayout()
    }


    private fun setupRecyclerView() {
        storeAdapter = StoreAdapter(productList) { product ->
            if (binding.tabLayout.selectedTabPosition == 4) {  //  MY 탭에서 선택한 경우
                selectedProductInMyTab = product
                Log.d("StoreFragment", "MY 탭에서 선택된 상품: ${product.name}")
            } else {
                selectedProduct = product
                applySelectedProductToStore(product)

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
                    itemType = productUiStateProduct.itemType ?: "UNKNOWN"
                )
            }
        )
    }
    private fun applySelectedProductToStore(product: Product) {
        Log.d("StoreFragment", "applySelectedProductToStore 호출 - 상품: ${product.name}, itemType: ${product.itemType}")

        when (product.itemType) {
            "SWING" -> binding.imgStoreSwingArea.post {
                Glide.with(binding.imgStoreSwingArea.context)
                    .load(product.imageUrl)
                    .into(binding.imgStoreSwing)
                Log.d("StoreFragment", "Swing 이미지 업데이트 완료")
            }

            "TOY" -> binding.imgStoreToyArea.post {
                Glide.with(binding.imgStoreToyArea.context)
                    .load(product.imageUrl)
                    .into(binding.imgStoreToy)
                Log.d("StoreFragment", "Toy 이미지 업데이트 완료")
            }

            "BOWL" -> binding.imgStoreBowlArea.post {
                Glide.with(binding.imgStoreBowlArea.context)
                    .load(product.imageUrl)
                    .into(binding.imgStoreBowl)
                Log.d("StoreFragment", "Bowl 이미지 업데이트 완료")
            }

            "NEST" -> binding.imgStoreNestArea.post {
                Glide.with(binding.imgStoreNestArea.context)
                    .load(product.imageUrl)
                    .into(binding.imgStoreNest)
                Log.d("StoreFragment", "Nest 이미지 업데이트 완료")
            }

            else -> Log.e("StoreFragment", "알 수 없는 itemType: ${product.itemType}")
        }
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
            layoutParams.marginStart =
                if (index == 0) 20.toPx(requireContext()) else 12.toPx(requireContext())
            layoutParams.marginEnd = 12.toPx(requireContext())
            tabView.layoutParams = layoutParams
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.customView?.let {
                    updateTabView(
                        it,
                        isSelected = true
                    )
                }                // 탭 커스텀 뷰 업데이트
                tab?.position?.let { position ->
                    val selectedCategory = when (position) {
                        0 -> ProductCategory.SWING
                        1 -> ProductCategory.TOY
                        2 -> ProductCategory.BOWL
                        3 -> ProductCategory.NEST
                        else -> null
                    }

                    if (selectedCategory != null) {
                        Log.d(
                            "StoreFragment",
                            "Fetching data for category: ${selectedCategory.name}"
                        )
                        viewModel.fetchShopData(selectedCategory.name)//  API 다시 호출
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
                tab?.position?.let { position ->
                    val selectedCategory = when (position) {
                        0 -> ProductCategory.SWING
                        1 -> ProductCategory.TOY
                        2 -> ProductCategory.BOWL
                        3 -> ProductCategory.NEST
                        else -> null
                    }

                    if (selectedCategory != null) {
                        Log.d(
                            "StoreFragment",
                            "Re-fetching data for category: ${selectedCategory.name}"
                        )
                        viewModel.fetchShopData(selectedCategory.name) //같은 탭 다시 눌러도 데이터 로드
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
            setTextColor(
                ContextCompat.getColor(
                    context,
                    if (isSelected) R.color.main1 else R.color.black
                )
            )
            isSingleLine = true
        }
    }

    private fun showPurchaseDialog(product: Product) {

        if (binding == null) {
            Log.e("StoreFragment", "showPurchaseDialog() 실행 시 binding이 null입니다.")
            return
        }

        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_purchase_success, null)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) //배경을 투명하게 설정
        }
        val imgProduct = dialogView.findViewById<ImageView>(R.id.img_product)
        val tvProductName = dialogView.findViewById<TextView>(R.id.tv_product_name)
        val tvProductPrice = dialogView.findViewById<TextView>(R.id.tv_product_price)
        val btnConfirm = dialogView.findViewById<TextView>(R.id.tv_purchase_confirm)
        val btnCancel = dialogView.findViewById<TextView>(R.id.tv_purchase_cancel)

        Glide.with(imgProduct.context)
            .load(product.imageUrl) // 서버에서 받은 이미지 URL 사용
            .into(imgProduct) // 이미지뷰에 적용
        tvProductName.text = product.name
        tvProductPrice.text = product.price.toString() //  스웨거에서 받은 가격 적용

        btnConfirm.setOnClickListener {
            Log.d("StoreFragment", " ${product.name} 구매 버튼 클릭")

            viewModel.purchaseSkin(
                itemId = product.id,
                itemType = product.category.name,
                itemName = product.name,
                reward = product.price,
                homeViewModel = homeViewModel
            )

            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            Log.d("StoreFragment", "구매 취소됨")
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateProductList(newList: List<Product>, isMyTab: Boolean = false) {
        productList.clear()

        val updatedList = if (isMyTab) {
            myProducts.map { it.copy(itemType = "MY") } // MY 탭에서는 itemType을 "MY"로 설정
        } else {
            newList
        }

        productList.addAll(updatedList)

        productList.forEach { product ->
            Log.d("StoreFragment", "최종 Product 리스트 - id: ${product.id}, name: ${product.name}, itemType: ${product.itemType}")
        }

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

        val btnFailureCheck = dialogView.findViewById<View>(R.id.btn_failure_check) as? TextView

        if (btnFailureCheck == null) {
            Log.e("StoreFragment", " btnFailureCheck 찾을 수 없음. XML 레이아웃 ID 확인 필요!")
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
    private fun saveProductToMyTab(product: Product) {
        if (!myProducts.contains(product)) {
            myProducts.add(product)
            Log.d("StoreFragment", "${product.name}이 MY 탭에 추가됨")

            if (binding.tabLayout.selectedTabPosition == 4) {
                viewModel.fetchShopData("MY")
            }
        }
    }
}