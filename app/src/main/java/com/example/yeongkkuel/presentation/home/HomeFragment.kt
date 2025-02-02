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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentHomeBinding
import com.example.yeongkkuel.presentation.botsheet.BotSheetViewModel
import com.example.yeongkkuel.presentation.botsheet.BotSheetUiState
import com.example.yeongkkuel.presentation.home.store.Product
import com.example.yeongkkuel.presentation.home.store.StoreFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class HomeFragment : Fragment() {
    private lateinit var navController: NavController
    private var _binding: FragmentHomeBinding? = null
    private val binding: FragmentHomeBinding
        get() = requireNotNull(_binding) { "FragmentHomeBinding -> null" }
    private val PREFS_NAME = "AppPrefs"
    private val KEY_LAST_HIDDEN_DATE = "lastHiddenDate"

    // BotSheetViewModel을 참조
    private val botSheetViewModel: BotSheetViewModel by viewModels()
    override fun onResume() {
        super.onResume()
        updateWarningVisibility(botSheetViewModel.uiState.value)
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val showRewardModal = arguments?.getBoolean("showRewardModal") ?: false
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

        // FragmentResult API를 통해 StoreFragment에서 데이터 수신
        parentFragmentManager.setFragmentResultListener("selectedProductKey", this) { _, bundle ->
            val selectedProduct = bundle.getParcelable<Product>("selectedProduct")
            selectedProduct?.let {
                Log.d("HomeFragment", "Received selected product: ${it.name}")
                applySelectedProductToHome(it)
            }
        }

        setupSwipeToDismiss(binding.imgWarningStart)
        renderMyProductsForHome()

        // ✅ StateFlow를 collect 할 때 viewLifecycleOwner.lifecycleScope.launch 사용
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

        setupSwipeToDismiss(binding.imgWarningStart)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateWarningVisibility(uiState: BotSheetUiState) {
        val sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastHiddenDate = sharedPreferences.getString(KEY_LAST_HIDDEN_DATE, null)
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // 🔥 한 번 숨김 처리가 되었다면 다시 표시되지 않도록 설정
        if (lastHiddenDate == null) {
            sharedPreferences.edit().putString(KEY_LAST_HIDDEN_DATE, todayDate).apply()
        }

        val hasSpendingData = uiState.spendingList.isNotEmpty()
        val categoryList = botSheetViewModel.getCategoryList()
        val hasCategories = categoryList.isNotEmpty()

        Log.d("HomeFragment", "📌 lastHiddenDate 확인: $lastHiddenDate, todayDate: $todayDate")
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
        val homeImageResId = mapToHomeResource(product.imageResId)

        when (product.area) {
            "Swing Area" -> binding.imgHomeSwing.post {
                binding.imgHomeSwing.setImageResource(homeImageResId)
                Log.d("HomeFragment", "Swing Image Updated: $homeImageResId")
            }
            "Toy Area" -> binding.imgHomeToy.post {
                binding.imgHomeToy.setImageResource(homeImageResId)
                Log.d("HomeFragment", "Toy Image Updated: $homeImageResId")
            }
            "Bowl Area" -> binding.imgHomeBowl.post {
                binding.imgHomeBowl.setImageResource(homeImageResId)
                Log.d("HomeFragment", "Bowl Image Updated: $homeImageResId")
            }
            "Nest Area" -> binding.imgHomeNest.post {
                binding.imgHomeNest.setImageResource(homeImageResId)
                Log.d("HomeFragment", "Nest Image Updated: $homeImageResId")
            }
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
        val myProducts = StoreFragment.myProducts.map { product ->
            product.copy(
                imageResId = mapToHomeResource(product.imageResId) // Home 리소스로 변환
            )
        }

        myProducts.forEach { product ->
            when (product.area) {
                "Swing Area" -> binding.imgHomeSwing.setImageResource(product.imageResId)
                "Toy Area" -> binding.imgHomeToy.setImageResource(product.imageResId)
                "Bowl Area" -> binding.imgHomeBowl.setImageResource(product.imageResId)
                "Nest Area" -> binding.imgHomeNest.setImageResource(product.imageResId)
            }
        }
    }

    private fun mapToHomeResource(storeResourceId: Int): Int {
        return when (storeResourceId) {
            R.drawable.img_product_swing1 -> R.drawable.img_home_swing1
            R.drawable.img_product_swing2 -> R.drawable.img_home_swing2
            R.drawable.img_product_toy1 -> R.drawable.img_home_toy1
            R.drawable.img_product_toy2 -> R.drawable.img_home_toy2
            R.drawable.img_product_bowl1 -> R.drawable.img_home_bowl1
            R.drawable.img_product_bowl2 -> R.drawable.img_home_bowl2
            R.drawable.img_product_nest1 -> R.drawable.img_home_nest1
            R.drawable.img_product_nest2 -> R.drawable.img_home_nest2
            else -> storeResourceId
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

}
