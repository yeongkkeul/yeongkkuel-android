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
import com.example.yeongkkuel.presentation.home.store.data.ShopItem
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

                // ✅ 스킨 착용 API 호출 (뷰모델을 통해 실행)
                viewModel.equipSkin(product.id)

                // UI 반영
                Toast.makeText(requireContext(), "${product.name}이 착용되었습니다!", Toast.LENGTH_SHORT).show()
                Log.d("StoreFragment", "${product.name} 착용 완료")
            } ?: Log.d("StoreFragment", "선택된 상품 없음")
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
                Log.d("StoreFragment", "✅ 스킨 구매 성공 - 최신 데이터 갱신 요청")

            } else {
                Log.e("StoreFragment", "❌ 스킨 구매 실패: ${response?.message ?: "서버 응답 없음"}")

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
            Log.d("StoreFragment", "🟢 API Response: $response") // 전체 응답 출력

            if (response?.isSuccess == true) {
                val mySkinList = response.result.itemList
                Log.d("StoreFragment", "✅ MY 탭 최신 데이터 - ${mySkinList.size}개 아이템")

                if (mySkinList.isEmpty()) {
                    Log.e("StoreFragment", "❌ MY 스킨 목록이 비어 있음! 서버 응답 확인 필요")
                }

                mySkinList.forEach { shopItem ->
                    Log.d("StoreFragment", "📌 MY 탭 아이템 - id: ${shopItem.id}, name: ${shopItem.itemName}, itemType: ${shopItem.itemType ?: "UNKNOWN"}")
                }

                updateProductList(mySkinList.map { shopItem ->
                    Product(
                        id = shopItem.id,
                        name = shopItem.itemName,
                        price = shopItem.price ?: 0,
                        category = ProductCategory.MY,
                        imageUrl = shopItem.itemImg ?: "",
                        itemType = shopItem.itemType ?: "MY"
                    )
                }, isMyTab = true)
            }
        }

        viewModel.fetchShopData("SWING") //  초기 데이터 로드

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
        val existingProduct = myProducts.find { it.id == product.id }

        if (existingProduct == null) {  // ✅ 중복 방지
            myProducts.add(product)
            Log.d("StoreFragment", "✅ ${product.name} (ID: ${product.id})이 MY 탭에 추가됨")

            if (binding.tabLayout.selectedTabPosition == 4) {
                viewModel.fetchShopData("MY")
            }
        } else {
            Log.d("StoreFragment", "⚠️ ${product.name} (ID: ${product.id}) 이미 MY 탭에 존재함")
        }
    }
    /**
     * ✅ 스킨 착용 API 호출 함수 (MY 탭에서 스킨을 착용할 때 사용)
     */
//    private fun equipSkin(purchaseId: Int) {
//        val requestBody = mapOf(
//            "userItem" to listOf(mapOf("purchaseId" to purchaseId))
//        )
//
//        viewModel.equipSkin(requestBody) // ✅ API 호출 (observe 사용 X)
//
//        // ✅ 응답을 ViewModel의 LiveData에서 감지
//        viewModel.equipResponse.observe(viewLifecycleOwner) { response ->
//            if (response?.isSuccess == true) {
//                Log.d("StoreFragment", "스킨 착용 성공: ${response.message}")
//
//                Toast.makeText(requireContext(), "스킨이 착용되었습니다!", Toast.LENGTH_SHORT).show()
//
//                // ✅ 착용 후 MY 탭 데이터 갱신
//                viewModel.fetchShopData("MY")
//            } else {
//                Log.e("StoreFragment", "스킨 착용 실패: ${response?.message ?: "서버 응답 없음"}")
//                Toast.makeText(requireContext(), "스킨 착용 실패!", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

}