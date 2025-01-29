package com.example.yeongkkuel.presentation.home

import android.app.Dialog
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
import kotlinx.coroutines.launch


class HomeFragment : Fragment() {
    private lateinit var navController: NavController
    private var _binding: FragmentHomeBinding? = null
    private val binding: FragmentHomeBinding
        get() = requireNotNull(_binding) { "FragmentHomeBinding -> null" }

    // BotSheetViewModel을 참조
    private val botSheetViewModel: BotSheetViewModel by viewModels()

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

        val selectedProduct = arguments?.getParcelable<Product>("selectedProduct")
        selectedProduct?.let {
            Log.d("HomeFragment", "Applying selected product to Home: ${it.name}")
            applySelectedProductToHome(it)
        }

        // MY 상품을 Home 화면에 렌더링
        view.post {
            renderMyProductsForHome()
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

        // StateFlow를 collect로 관찰하기
        lifecycleScope.launch {
            botSheetViewModel.uiState.collect { uiState ->
                updateWarningVisibility(uiState)
            }
        }

        // imgWarningStart 스와이프 동작 설정
        setupSwipeToDismiss(binding.imgWarningStart)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateWarningVisibility(uiState: BotSheetUiState) {
        val isEmpty = uiState.spendingList.isEmpty()
        binding.imgWarningStart.visibility = if (isEmpty) View.VISIBLE else View.GONE
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
}
