package com.example.yeongkkuel.presentation.home

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentHomeBinding
import com.example.yeongkkuel.network.response.expenditure.MonthExpendituresCategory
import com.example.yeongkkuel.presentation.botsheet.BotSheetViewModel
import com.example.yeongkkuel.presentation.botsheet.BotSheetUiState
import com.example.yeongkkuel.presentation.home.category.CategoryViewModel
import com.example.yeongkkuel.presentation.home.category.data.Category
import com.example.yeongkkuel.presentation.home.store.Product
import com.example.yeongkkuel.presentation.home.store.StoreFragment
//import com.example.yeongkkuel.presentation.home.store.data.MySkin
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.bumptech.glide.Glide


class HomeFragment : Fragment() {
    private lateinit var navController: NavController
    private var _binding: FragmentHomeBinding? = null
    private val binding: FragmentHomeBinding
        get() = requireNotNull(_binding) { "FragmentHomeBinding -> null" }
    private val PREFS_NAME = "AppPrefs"
    private val KEY_LAST_HIDDEN_DATE = "lastHiddenDate"
    private lateinit var repository: HomeRepository

    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModel.Factory(HomeRepository())
    }
    private val categoryViewModel: CategoryViewModel by viewModels()
    private val botSheetViewModel: BotSheetViewModel by activityViewModels()


    override fun onResume() {
        super.onResume()

        Log.d("HomeFragment", "✅ onResume() - getSpendingList() 실행됨") // 디버깅용 로그 추가
        botSheetViewModel.getSpendingList()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val showRewardModal = arguments?.getBoolean("showRewardModal") ?: false

        repository = HomeRepository()
        if (showRewardModal) {
            showRewardDialog()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        val sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastHiddenDate = sharedPreferences.getString(KEY_LAST_HIDDEN_DATE, "")
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        if (lastHiddenDate == todayDate) {
            binding.imgWarningStart.visibility = View.GONE
        }

        // StoreFragment에서 선택한 상품을 수신하여 홈 화면 업데이트
        parentFragmentManager.setFragmentResultListener("selectedProductKey", this) { _, bundle ->
            val selectedProduct = bundle.getParcelable<Product>("selectedProduct")
            selectedProduct?.let {
                Log.d("HomeFragment", "✅ StoreFragment에서 받은 상품: ${it.name}, area: ${it.area}, imageUrl: ${it.imageUrl}")
                applySelectedProductToHome(it)
            } ?: Log.e("HomeFragment", "❌ StoreFragment에서 받은 상품이 null입니다!")
        }
        setupSwipeToDismiss(binding.imgWarningStart)
        renderMyProductsForHome()


        // StateFlow를 collect 할 때 viewLifecycleOwner.lifecycleScope.launch 사용
        viewLifecycleOwner.lifecycleScope.launch {
            botSheetViewModel.uiState.collectLatest { uiState ->
                updateWarningVisibility(uiState)
            }
        }

        // StoreFragment로 이동
        binding.imgHomeStore.setOnClickListener {
            binding.bgHomeStore.visibility = View.GONE
            binding.bgHomeStoreClick.visibility = View.VISIBLE
            navController.navigate(R.id.action_homeFragment_to_storeFragment)
        }

        // CategoryManageFragment로 이동
        val ivHamberger = requireActivity().findViewById<ImageView>(R.id.iv_hamberger)
        ivHamberger.setOnClickListener {
            navController.navigate(R.id.categoryManageFragment)
        }
        homeViewModel.homeResponse.observe(viewLifecycleOwner) { response ->
            if (response != null && response.isSuccess) {
                Log.d("HomeFragment", "✅ 홈 데이터 수신 완료: ${response.result}")
                binding.tvCoin.text = response.result.myReward.toString() // ✅ 숫자만 표시
                updateMySkins(response.result.mySkin)
            } else {
                Log.e("HomeFragment", "🚨 홈 데이터 수신 실패 또는 응답 없음!")
            }
        }
        setupSwipeToDismiss(binding.imgWarningStart)

        Log.d("HomeFragment", "🚀 fetchHomeData() 호출됨!") // ✅ 로그 추가

        // ✅ 중복 실행 방지: 최초 실행 여부 체크
        if (savedInstanceState == null) {
            homeViewModel.fetchHomeData()
        }
        // ✅ Null 체크 추가
        parentFragmentManager.setFragmentResultListener("selectedProductKey", this) { _, bundle ->
            if (bundle.containsKey("selectedProduct")) {
                bundle.getParcelable<Product>("selectedProduct")?.let {
                    Log.d("HomeFragment", "Received selected product: ${it.name}")
                    applySelectedProductToHome(it)
                }
            }
        }
//        homeViewModel.homeResponse.observe(viewLifecycleOwner) { response ->
//            if (response != null && response.isSuccess) {
//                Log.d("HomeFragment", "✅ 홈 데이터 정상 수신: $response")
//
//                binding.tvCoin.text = "보유 리워드: ${response.result.myReward}"
//                updateMySkins(response.result.mySkin)
//
//                // ✅ 여기서 `toCategory(categoryViewModel)`로 변경!
//                val categories = response.result.categories.map { it.toCategory(categoryViewModel) }
//                val expensesMap = response.result.categories.associate { it.categoryId to it.expenses }
//
//                updateCategoryExpenses(categories, expensesMap)
//
//                Log.d("HomeFragment", "🚀 updateBotSheetCategories 호출됨!")
//                botSheetViewModel.updateBotSheetCategories(categories)
//            } else {
//                Log.e("HomeFragment", "🚨 홈 데이터 불러오기 실패 또는 응답 없음!")
//            }
//        }
    }

    // ✅ 변환된 Category 리스트를 받도록 변경
    private fun updateCategoryExpenses(categories: List<Category>, expensesMap: Map<Int, List<Expense>>) {
        categories.forEach { category ->
            val expenses = expensesMap[category.id] ?: emptyList() // ✅ 카테고리에 해당하는 지출 내역 가져오기

            expenses.forEach { expense ->
                Log.d("HomeFragment", "📌 카테고리: ${category.name}, 지출: ${expense.content}, 금액: ${expense.amount}")
            }
        }
    }


    private fun getDrawableFromUrl(url: String): Int {
        return when (url) {
            "swing_1.png" -> R.drawable.img_home_swing1
            "toy_1.png" -> R.drawable.img_home_toy1
            "bowl_1.png" -> R.drawable.img_home_bowl1
            "nest_1.png" -> R.drawable.img_home_nest1
            else -> R.drawable.img_home_nest1
        }
    }

    private fun updateMySkins(mySkins: List<MySkin>) {
        mySkins.forEach { skin ->
            val imageResId = getDrawableFromUrl(skin.imgUrl)

            when (skin.itemType) {
                "SWING" -> binding.imgHomeSwing.setImageResource(imageResId)
                "TOY" -> binding.imgHomeToy.setImageResource(imageResId)
                "BOWL" -> binding.imgHomeBowl.setImageResource(imageResId)
                "NEST" -> binding.imgHomeNest.setImageResource(imageResId)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateWarningVisibility(uiState: BotSheetUiState) {
        val sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastHiddenDate = sharedPreferences.getString(KEY_LAST_HIDDEN_DATE, null)
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        if (lastHiddenDate == null) {
            sharedPreferences.edit().putString(KEY_LAST_HIDDEN_DATE, todayDate).apply()
        }

        val hasSpendingData = uiState.spendingList.isNotEmpty()
        // `_uiState.value.spendingList`를 바로 사용해서 카테고리 개수 확인
        val categoryList = uiState.spendingList.map { spending ->
            Category(
                id = spending.categoryId,
                name = spending.kind.name,
                color = spending.color
            )
        }
        val hasCategories = categoryList.isNotEmpty()

        Log.d("HomeFragment", "📌 현재 카테고리 개수: ${categoryList.size}, 지출 내역 개수: ${uiState.spendingList.size}")

        binding.imgWarningStart.post {
            if (lastHiddenDate == todayDate) {
                // 🔹 오늘 한 번 숨긴 경우 -> 계속 숨김 유지
                binding.imgWarningStart.visibility = View.GONE
                Log.d("HomeFragment", "🚨 오늘 이미 숨김 처리됨 -> imgWarningStart 숨기기")
            } else {
                // 🔹 카테고리가 없거나 지출 내역이 없을 때만 보이도록 설정
                binding.imgWarningStart.visibility = if (hasSpendingData || hasCategories) View.GONE else View.VISIBLE
                Log.d(
                    "HomeFragment",
                    if (hasSpendingData || hasCategories) "✅ 지출 내역 또는 카테고리 있음 -> imgWarningStart 숨기기"
                    else "❗ 지출 내역 및 카테고리 없음 -> imgWarningStart 보이기"
                )
            }
        }
    }

    private fun setupSwipeToDismiss(view: View) {
        var startY = 0f
        var isSwipingDown = false

        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startY = event.y
                    isSwipingDown = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaY = event.y - startY
                    if (deltaY > 100) {
                        isSwipingDown = true
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isSwipingDown) {
                        v.animate()
                            .translationY(v.height.toFloat())
                            .alpha(0f)
                            .setDuration(300)
                            .withEndAction {
                                v.visibility = View.GONE
                                saveHiddenDate()  // 🔹 숨긴 날짜 저장
                            }
                            .start()
                    } else {
                        v.performClick()
                    }
                    true
                }
                else -> false
            }
        }
    }
    private fun applySelectedProductToHome(product: Product) {
        Log.d("HomeFragment", "🎨 applySelectedProductToHome 호출 - 상품: ${product.name}, area: ${product.area}, imageUrl: ${product.imageUrl}")

        when (product.area) {
            "Swing Area" -> binding.imgHomeSwing.post {
                Glide.with(binding.imgHomeSwing.context)
                    .load(product.imageUrl)
                    .into(binding.imgHomeSwing)
                Log.d("HomeFragment", "✅ Swing Image 업데이트 완료: ${product.imageUrl}")
            }
            "Toy Area" -> binding.imgHomeToy.post {
                Glide.with(binding.imgHomeToy.context)
                    .load(product.imageUrl)
                    .into(binding.imgHomeToy)
                Log.d("HomeFragment", "✅ Toy Image 업데이트 완료: ${product.imageUrl}")
            }
            "Bowl Area" -> binding.imgHomeBowl.post {
                Glide.with(binding.imgHomeBowl.context)
                    .load(product.imageUrl)
                    .into(binding.imgHomeBowl)
                Log.d("HomeFragment", "✅ Bowl Image 업데이트 완료: ${product.imageUrl}")
            }
            "Nest Area" -> binding.imgHomeNest.post {
                Glide.with(binding.imgHomeNest.context)
                    .load(product.imageUrl)
                    .into(binding.imgHomeNest)
                Log.d("HomeFragment", "✅ Nest Image 업데이트 완료: ${product.imageUrl}")
            }
            else -> Log.e("HomeFragment", "❌ 알 수 없는 area: ${product.area}, imageUrl: ${product.imageUrl}")
        }
    }

    private fun showRewardDialog() {
        val dialog = Dialog(requireContext())
        dialog.show()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        val params = dialog.window?.attributes
        params?.dimAmount = 0.65f
        dialog.window?.attributes = params

        dialog.setContentView(R.layout.dialog_reward)
        dialog.setCancelable(true)

        val btnClose = dialog.findViewById<View>(R.id.tv_close)
        val btnReward = dialog.findViewById<View>(R.id.tv_reward)

        btnClose.setOnClickListener {
            dialog.dismiss()
        }
        dialog.window?.apply {
            setBackgroundDrawableResource(R.drawable.ic_store_topurchase) // VectorDrawable 설정
            decorView.clipToOutline = true // 💡 둥근 모서리 적용
        }
        dialog.show()
    }

    /**
     * MY 상품을 Home 화면에 렌더링
     */
    private fun renderMyProductsForHome() {
        val myProducts = StoreFragment.myProducts
        Log.d("HomeFragment", "🛒 MY 탭에서 가져온 상품 리스트: ${myProducts.size}개")

        if (myProducts.isEmpty()) {
            Log.e("HomeFragment", "❌ MY 탭에 저장된 상품이 없습니다!")
        }

        myProducts.forEach { product ->
            // ✅ `area` 값이 null이면 `itemType`을 기반으로 기본값 설정
            val area = product.area ?: when (product.itemType) {
                "SWING" -> "Swing Area"
                "TOY" -> "Toy Area"
                "BOWL" -> "Bowl Area"
                "NEST" -> "Nest Area"
                else -> "Unknown Area"
            }

            Log.d("HomeFragment", "🛠 ${area}에 적용할 상품: ${product.name}, imageUrl: ${product.imageUrl}")

            when (area) {
                "Swing Area" -> Glide.with(binding.imgHomeSwing.context)
                    .load(product.imageUrl)
                    .into(binding.imgHomeSwing)
                    .also { Log.d("HomeFragment", "✅ Swing Image Updated: ${product.imageUrl}") }

                "Toy Area" -> Glide.with(binding.imgHomeToy.context)
                    .load(product.imageUrl)
                    .into(binding.imgHomeToy)
                    .also { Log.d("HomeFragment", "✅ Toy Image Updated: ${product.imageUrl}") }

                "Bowl Area" -> Glide.with(binding.imgHomeBowl.context)
                    .load(product.imageUrl)
                    .into(binding.imgHomeBowl)
                    .also { Log.d("HomeFragment", "✅ Bowl Image Updated: ${product.imageUrl}") }

                "Nest Area" -> Glide.with(binding.imgHomeNest.context)
                    .load(product.imageUrl)
                    .into(binding.imgHomeNest)
                    .also { Log.d("HomeFragment", "✅ Nest Image Updated: ${product.imageUrl}") }

                else -> Log.e("HomeFragment", "❌ 올바르지 않은 area 값: $area")
            }
        }
    }


    //    private fun mapToHomeResource(storeResourceId: Int): Int {
//        return when (storeResourceId) {
//            R.drawable.img_product_swing1 -> R.drawable.img_home_swing1
//            R.drawable.img_product_swing2 -> R.drawable.img_home_swing2
//            R.drawable.img_product_toy1 -> R.drawable.img_home_toy1
//            R.drawable.img_product_toy2 -> R.drawable.img_home_toy2
//            R.drawable.img_product_bowl1 -> R.drawable.img_home_bowl1
//            R.drawable.img_product_bowl2 -> R.drawable.img_home_bowl2
//            R.drawable.img_product_nest1 -> R.drawable.img_home_nest1
//            R.drawable.img_product_nest2 -> R.drawable.img_home_nest2
//            else -> storeResourceId
//        }
//    }
    private fun saveHiddenDate() {
        val sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        Log.d("HomeFragment", "🔹 saveHiddenDate() 실행됨, 저장 날짜: $todayDate") // ✅ 로그 추가

        sharedPreferences.edit()
            .putString(KEY_LAST_HIDDEN_DATE, todayDate)
            .apply()
    }

}