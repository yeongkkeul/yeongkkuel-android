package com.example.yeongkkuel.presentation.base

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.ActivityMainBinding
import com.example.yeongkkuel.presentation.botsheet.BotSheetCategoryListAdapter
import com.example.yeongkkuel.presentation.botsheet.BotSheetItemTouchHelper
import com.example.yeongkkuel.presentation.botsheet.BotSheetListener
import com.example.yeongkkuel.presentation.botsheet.BotSheetUiState
import com.example.yeongkkuel.presentation.botsheet.BotSheetViewModel
import com.example.yeongkkuel.presentation.home.category.CategoryViewModel
import com.example.yeongkkuel.presentation.util.dpToPx
import com.example.yeongkkuel.presentation.util.toNaviStat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), BotSheetListener {
    private lateinit var binding: ActivityMainBinding

    private val botSheetViewModel: BotSheetViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()

    private val botSheetCategoryListAdapter by lazy {
        BotSheetCategoryListAdapter(this)
    }

    private var rvBottomSheetCollapseStateHeight: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {


        // 스플래시 화면 설정
        val splashScreen = this.installSplashScreen()

        splashScreen.setOnExitAnimationListener { splashScreenView ->
            splashScreenView.iconView.animate()
                .translationY(-splashScreenView.iconView.height.toFloat())
                .setDuration(10)
                .withEndAction {
                    splashScreenView.remove()
                }
                .start()
        }

        super.onCreate(savedInstanceState)

        Timber.plant(Timber.DebugTree())

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 스플래시 화면 종료 조건 설정 (예: 데이터 초기화 완료)
        splashScreen.setKeepOnScreenCondition {
            // 앱 초기화 작업이 완료될 때까지 유지
            checkInitialization()
        }

        setupHamburgerClickListener() // 카테고리 더보기 기능 추가

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment

        val navController = navHostFragment.navController
        setupAddCategoryClickListener(navController)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initView()
        initViewModel()
    }

    private fun setupAddCategoryClickListener(navController: NavController) {
        binding.tvAddCategory.setOnClickListener {
            navController.navigate(R.id.categoryAddFragment, null, NavOptions.Builder()
                .setLaunchSingleTop(true) // 이미 존재하면 새로 생성하지 않음
                .setRestoreState(true) // 상태 복원
                .build()
            )
        }
    }

    private fun initView() = with(binding) {
        fun initBottomSheet() {
            val bottomSheet = binding.clItemBotSheet
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

            val displayHeight = resources.displayMetrics.heightPixels

            rvBottomSheetCollapseStateHeight = displayHeight - 500.dpToPx(this@MainActivity)
            val peekHeight = rvBottomSheetCollapseStateHeight + 80.dpToPx(this@MainActivity)

            // BottomSheet 초기 상태 설정
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            bottomSheetBehavior.peekHeight = peekHeight

            // RecyclerView 초기 높이 설정

            binding.rvBotSheetCategory.layoutParams.height = rvBottomSheetCollapseStateHeight
            binding.rvBotSheetCategory.requestLayout()

            // BottomSheet 이벤트 핸들링
            bottomSheetBehavior.addBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            val collapsedHeight = rvBottomSheetCollapseStateHeight
                            updateRecyclerViewHeight(collapsedHeight)
                        }

                        BottomSheetBehavior.STATE_EXPANDED -> {
                            val expandedHeight = displayHeight - 140.dpToPx(this@MainActivity)
                            updateRecyclerViewHeight(expandedHeight)
                        }

                        else -> {
                            // 기타 상태 처리 (예: 드래그 상태 등)
                        }
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                }
            })

            rvBotSheetCategory.run {
                adapter = botSheetCategoryListAdapter
                layoutManager = LinearLayoutManager(this@MainActivity)

                // RecyclerView 터치 중에는 BottomSheet가 터치 이벤트를 받지 않도록 설정
                setOnTouchListener { _, _ ->
                    // RecyclerView가 터치될 때 BottomSheet가 드래그되지 않도록 설정
                    bottomSheetBehavior.isDraggable = false // BottomSheet 드래그 비활성화
                    bottomSheet.requestDisallowInterceptTouchEvent(true) // BottomSheet가 터치 이벤트를 받지 않도록 설정
                    false // RecyclerView의 터치 이벤트를 처리하도록 함
                }

                // RecyclerView 터치가 끝나면 BottomSheet가 다시 터치 가능한 상태로 복원
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            // RecyclerView가 스크롤이 멈추면 BottomSheet가 터치 가능하도록 복원
                            bottomSheet.requestDisallowInterceptTouchEvent(false) // 터치 이벤트 복원
                            bottomSheetBehavior.isDraggable = true // BottomSheet 드래그 활성화
                        }
                    }
                })

                // 드래그로 아이템 이동
                val itemTouchHelper = ItemTouchHelper(
                    BotSheetItemTouchHelper(
                        onMove = { fromPosition, toPosition ->
                            botSheetViewModel.moveCategory(
                                fromPosition = fromPosition,
                                toPosition = toPosition
                            )
                        })
                )
                itemTouchHelper.attachToRecyclerView(this@run)
            }

            //botSheet 상단 날짜
            val dateFormatSheet = SimpleDateFormat("MM월 dd일 E요일", Locale.KOREAN)
            val formattedDateSheet = dateFormatSheet.format(Date())
            tvBottomSheetDate.text = formattedDateSheet
        }

        fun initNav() {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment

            val navController = navHostFragment.navController

            // BottomNavigationView 설정
            binding.bottomNavi.setupWithNavController(navController)

            // BottomNavigationView 아이템 선택 리스너 설정
            binding.bottomNavi.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_home -> {
                        navController.navigate(R.id.navigation_home)
                        true
                    }

                    R.id.navigation_chat -> {
                        navController.navigate(R.id.navigation_chat)
                        true
                    }

                    R.id.navigation_stat -> {
                        navController.navigate(R.id.navigation_stat)
                        true
                    }

                    R.id.navigation_my -> {
                        navController.navigate(R.id.navigation_my)
                        true
                    }

                    else -> false
                }
            }

            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.navigation_home,
                    R.id.navigation_stat,
                        -> setBotSheetVisible()


                    else -> setBotSheetGone()
                }
            }
            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.categoryAddFragment -> {
                        binding.tvAddCategory.visibility = View.GONE // 카테고리 추가 화면에서는 숨기기
                    }
                    R.id.navigation_home -> {
                        // 홈 화면 복귀 시 카테고리 개수 조건 확인
                        val isCategoryEmpty = categoryViewModel.categories.value.orEmpty().size < 1
                        if (isCategoryEmpty) {
                            binding.tvAddCategory.visibility = View.VISIBLE // 카테고리 없을 때 보이기
                        } else {
                            binding.tvAddCategory.visibility = View.GONE // 카테고리 있을 때 숨기기
                        }
                    }
                    else -> {
                        binding.tvAddCategory.visibility = View.GONE // 다른 화면에서는 숨기기
                        // TODO - * 지출 화면일 때 카테고리 추가 안 한 상태는 VISIBLE 상태로 만들기 *
                    }
                }
            }


            // 바텀네비게이션 뷰 숨김 처리 - 스플래시, 로그인
            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.navigation_splash,
                    R.id.navigation_login,
                    R.id.navigation_signup,
                    R.id.navigation_stat_setting,
                    R.id.navigation_stat_recommendation,
                    R.id.navigation_terms_agree,
                    R.id.navigation_term_1,
                    R.id.navigation_term_2,
                    R.id.navigation_term_3,
                    R.id.navigation_term_4 -> hideBottomNavigation(
                        true
                    )


                    else -> hideBottomNavigation(false) // 추가 처리
                }
            }

        }

        fun initBack() {
            val backPressedCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // NavHostFragment를 통해 NavController 가져오기
                    val navHostFragment =
                        supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
                    val navController = navHostFragment.navController

                    // 이전 백 스택 항목이 navigation_stat인지 확인
                    val previousBackStackEntry = navController.previousBackStackEntry
                    if (previousBackStackEntry?.destination?.id == R.id.navigation_stat) {
                        navController.toNaviStat()
                    } else if (previousBackStackEntry != null) {
                        navController.popBackStack()
                    } else {
                        finish()
                    }
                }
            }

            onBackPressedDispatcher.addCallback(this@MainActivity, backPressedCallback)
        }


        initBottomSheet()
        initNav()
        initBack()
    }

    fun initViewModel() = with(botSheetViewModel) {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController

        lifecycleScope.launch {
            uiState.flowWithLifecycle(lifecycle)
                .collectLatest { uiState ->
                    onBind(uiState) // ✅ UI 데이터 바인딩
                    botSheetCategoryListAdapter.submitList(uiState.spendingList) // ✅ 리스트 업데이트
                }
        }
    }

    private fun updateRecyclerViewHeight(newHeight: Int) {
        binding.rvBotSheetCategory.layoutParams = binding.rvBotSheetCategory.layoutParams.apply {
            height = newHeight
        }
        binding.rvBotSheetCategory.requestLayout()
    }


    fun hideBottomNavigation(state: Boolean) {
        if (state) binding.bottomNavi.visibility = View.GONE else binding.bottomNavi.visibility =
            View.VISIBLE
    }

    private fun checkInitialization(): Boolean {
        return false // false를 반환하면 스플래시 화면 종료
    }

    private fun setupHamburgerClickListener() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController

        // iv_hamberger 클릭 리스너 추가
        binding.ivHamberger.setOnClickListener {
            navController.navigate(R.id.categoryManageFragment)
            val bottomSheetBehavior = BottomSheetBehavior.from(binding.clItemBotSheet)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED // BottomSheet 닫기
        }
    }

    private fun onBind(uiState: BotSheetUiState) = with(binding) {

        botSheetCategoryListAdapter.submitList(uiState.spendingList)

        val isEmpty = uiState.spendingList.isEmpty()

        // 데이터 존재 여부에 따라 RecyclerView visibility 변경
        binding.rvBotSheetCategory.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.ivHamberger.visibility = if (isEmpty) View.GONE else View.VISIBLE

        // 데이터가 없으면 빈 메시지와 이미지 보이기, 있으면 숨기기
        binding.tvEmptyMessage1.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.tvEmptyMessage2.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.imgAddCategory.visibility = if (isEmpty) View.VISIBLE else View.GONE

    }

    override fun setPeekHeight(peekHeight: Int) {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.clItemBotSheet)
        bottomSheetBehavior.peekHeight = peekHeight

        rvBottomSheetCollapseStateHeight = peekHeight - 60.dpToPx(this@MainActivity)
        updateRecyclerViewHeight(rvBottomSheetCollapseStateHeight)
    }

    override fun setBotSheetGone() {
        binding.clItemBotSheet.visibility = View.GONE

        val bottomSheetBehavior = BottomSheetBehavior.from(binding.clItemBotSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    override fun setBotSheetVisible() {
        binding.clItemBotSheet.visibility = View.VISIBLE

        val bottomSheetBehavior = BottomSheetBehavior.from(binding.clItemBotSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    override fun navigateToExpenseEntry(selectedCategory: String, categoryColor: Int) {
        // NavController를 이용해 지출 기입 페이지로 이동
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController

        // 번들에 데이터를 담아 지출 기입 페이지로 전달
        val bundle = Bundle().apply {
            putString("selectedCategory", selectedCategory)
            putInt("categoryColor", categoryColor)
        }

        // 지출 기입 페이지로 이동하면서 데이터 전달
        navController.navigate(R.id.expenseEntryFragment, bundle)
    }

    override fun navigateToExpenseView(expenseName: String, expensePrice: Int, categoryColor: Int) {
        Log.d("MainActivity", "📌 navigateToExpenseView() 호출됨")
        Log.d("MainActivity", "📌 전달된 데이터 - name: $expenseName, price: $expensePrice, categoryColor: $categoryColor")

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController

        // 🔹 번들에 데이터 추가하여 ExpenseViewFragment로 전달
        val bundle = Bundle().apply {
            putString("expenseName", expenseName)
            putInt("expensePrice", expensePrice)
            putInt("categoryColor", categoryColor) // ✅ categoryColor를 Int로 전달
        }

        navController.navigate(R.id.navigation_expense_view, bundle)
    }
    override fun onNoExpenseChanged(isNoExpense: Boolean) {
    }


    override fun navigateToCategoryAddFragment() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController

        navController.navigate(R.id.categoryAddFragment)
    }

}