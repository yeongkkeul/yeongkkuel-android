package com.example.yeongkkuel.presentation.home

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
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
import com.example.yeongkkuel.presentation.base.MainActivity
import com.example.yeongkkuel.presentation.my.NotificationViewModel
import com.example.yeongkkuel.presentation.util.dpToPx
import java.util.Calendar

class HomeFragment : Fragment() {
    private lateinit var navController: NavController
    private var _binding: FragmentHomeBinding? = null
    private val binding: FragmentHomeBinding
        get() = requireNotNull(_binding) { "FragmentHomeBinding -> null" }
    private val PREFS_NAME = "AppPrefs"
    private val KEY_LAST_HIDDEN_DATE = "lastHiddenDate"
    private lateinit var repository: HomeRepository

    private val notificationViewModel: NotificationViewModel by viewModels()

    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModel.Factory(HomeRepository())
    }
    private val categoryViewModel: CategoryViewModel by viewModels()
    private val botSheetViewModel: BotSheetViewModel by activityViewModels()


    override fun onResume() {
        super.onResume()
//        botSheetViewModel.getSpendingList()

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
            homeViewModel.fetchYesterdayReward() // ✅ API 호출

            homeViewModel.yesterdayReward.observe(viewLifecycleOwner) { reward ->
                showRewardDialog(reward ?: 0) // ✅ reward 값 전달
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        botSheetViewModel.getSpendingList()
        scheduleMidnightRewardDialog()
        checkFirstLoginAfterMidnight()
        homeViewModel.yesterdayReward.observe(viewLifecycleOwner) { reward ->
            showRewardDialog(reward ?: 0) // ✅ null이면 기본값 0 전달
        }
        (activity as? MainActivity)?.resetBottomSheetState()

        // 홈 탭에 진입할 때 바텀시트 상태(피크 높이)를 재설정
        val displayHeight = resources.displayMetrics.heightPixels
        val desiredPeekHeight = displayHeight - 430.dpToPx(requireContext())
        (activity as? MainActivity)?.setPeekHeight(desiredPeekHeight)

        // StoreFragment에서 선택한 상품을 수신하여 홈 화면 업데이트
        parentFragmentManager.setFragmentResultListener("selectedProductKey", this) { _, bundle ->
            val selectedProduct = bundle.getParcelable<Product>("selectedProduct")
            selectedProduct?.let {
                Log.d("HomeFragment", "✅ StoreFragment에서 받은 상품: ${it.name}, area: ${it.area}, imageUrl: ${it.imageUrl}")
                applySelectedProductToHome(it)
            } ?: Log.e("HomeFragment", "❌ StoreFragment에서 받은 상품이 null입니다!")
        }

        setupSwipeToDismiss(binding.ivError)
        renderMyProductsForHome()

        // StateFlow collect
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
        homeViewModel.homeResult.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                Log.d("HomeFragment", "✅ 홈 데이터 수신 완료: $result")

                // 수정된 데이터 바인딩 방식
                binding.tvCoin.text = result.myReward.toString() // ✅ 숫자만 표시
                updateMySkins(result.mySkin) // ✅ 변경된 데이터 클래스 반영

                // 카테고리 정보 업데이트
                val categories = result.categories.map { it.toCategory(categoryViewModel) }
                val expensesMap = result.categories.associate { it.categoryId to it.expenses }
                updateCategoryExpenses(categories, expensesMap)

                Log.d("HomeFragment", "🚀 updateBotSheetCategories 호출됨!")
            } else {
                Log.e("HomeFragment", "🚨 홈 데이터 수신 실패 또는 응답 없음!")
            }
        }

        Log.d("HomeFragment", "🚀 fetchHomeData() 호출됨!")

        // 중복 실행 방지: 최초 실행 여부 체크
        if (savedInstanceState == null) {
            homeViewModel.fetchHomeData()
        }
        // Null 체크 추가
        parentFragmentManager.setFragmentResultListener("selectedProductKey", this) { _, bundle ->
            if (bundle.containsKey("selectedProduct")) {
                bundle.getParcelable<Product>("selectedProduct")?.let {
                    Log.d("HomeFragment", "Received selected product: ${it.name}")
                    applySelectedProductToHome(it)
                }
            }
        }

        // 읽지 않은 알림 확인


        initAppBar()
    }

    private fun initAppBar(){
        binding.includeTopbar.run {
            ivMore.visibility = View.GONE
            ivNoti.setOnClickListener {
                findNavController().navigate(R.id.navigation_notification)
            }


            val layoutParams = ivNoti.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.marginEnd = 12
            ivNoti.layoutParams = layoutParams
        }

        notificationViewModel.checkUnreadNotifications()
        notificationViewModel.unreadNotification.observe(viewLifecycleOwner) { hasUnread ->
            binding.includeTopbar.ivNotiDot.visibility =
                if (hasUnread) View.VISIBLE else View.INVISIBLE
        }

    }

    // 변환된 Category 리스트를 받도록 변경
    private fun updateCategoryExpenses(categories: List<Category>, expensesMap: Map<Int, List<Expense>>) {
        categories.forEach { category ->
            val expenses = expensesMap[category.id] ?: emptyList() // 카테고리에 해당하는 지출 내역 가져오기

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
        // 지출 내역 추가 여부를 판단: 하나라도 있으면 숨기고, 없으면 보이도록 함
        val hasExpense = uiState.spendingList.any { it.history.isNotEmpty() }
        if (hasExpense) {
            binding.ivError.visibility = View.GONE
        } else {
            // 지출 내역이 없으면 플로팅 알람 보이기 (애니메이션 적용)
            binding.ivError.visibility = View.VISIBLE
            binding.ivError.alpha = 0f
            binding.ivError.animate()
                .alpha(1f)
                .setDuration(300)
                .start()
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

    private fun showRewardDialog(yesterdayReward: Int) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        val params = dialog.window?.attributes
        params?.dimAmount = 0.65f
        dialog.window?.attributes = params

        dialog.setContentView(R.layout.dialog_reward_yesterday)
        dialog.setCancelable(true)

        val tvRewardText = dialog.findViewById<TextView>(R.id.tv_recommend_code_error)
        val btnClose = dialog.findViewById<View>(R.id.tv_close)
        val btnReward = dialog.findViewById<View>(R.id.tv_reward)

        tvRewardText.text = "어제 총 $yesterdayReward 리워드를 획득했어요."

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        btnReward.setOnClickListener {
            navController.navigate(R.id.navigation_reward) // ✅ 클릭 시 fragment_reward로 이동
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun scheduleMidnightRewardDialog() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val delay = calendar.timeInMillis - System.currentTimeMillis()

        if (delay > 0) {
            Handler(Looper.getMainLooper()).postDelayed({
                homeViewModel.fetchYesterdayReward()
            }, delay)
        }
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

    private fun saveHiddenDate() {
        val sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        Log.d("HomeFragment", "🔹 saveHiddenDate() 실행됨, 저장 날짜: $todayDate") // ✅ 로그 추가

        sharedPreferences.edit()
            .putString(KEY_LAST_HIDDEN_DATE, todayDate)
            .apply()
    }
    private fun checkFirstLoginAfterMidnight() {
        val sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val lastShownDate = sharedPreferences.getString("lastRewardDate", "")

        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        if (lastShownDate != todayDate) {
            homeViewModel.fetchYesterdayReward()
            sharedPreferences.edit().putString("lastRewardDate", todayDate).apply()
        }
    }


}