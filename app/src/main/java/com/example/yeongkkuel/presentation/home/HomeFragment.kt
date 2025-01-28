package com.example.yeongkkuel.presentation.home

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
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
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private lateinit var navController: NavController
    private var _binding: FragmentHomeBinding? = null
    private val binding: FragmentHomeBinding
        get() = requireNotNull(_binding){"FragmentHomeBinding -> null"}

    // BotSheetViewModel을 참조
    private val botSheetViewModel: BotSheetViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        // Arguments로 전달된 showRewardModal 값 확인
        val showRewardModal = arguments?.getBoolean("showRewardModal") ?: false
        if (showRewardModal) {
            showRewardDialog()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        val productIcons = arguments?.getIntArray("productIcons") ?: intArrayOf()
        if (productIcons.isNotEmpty()) {
            binding.productContainer.removeAllViews() // 기존 뷰 초기화
            productIcons.forEach { iconResId ->
                val imageView = ImageView(requireContext()).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setImageResource(iconResId)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
                binding.productContainer.addView(imageView)
            }
        }
        binding.imgHomeStore.setOnClickListener {
            binding.bgHomeStore.visibility = View.GONE
            binding.bgHomeStoreClick.visibility = View.VISIBLE
            navController.navigate(R.id.action_homeFragment_to_storeFragment)
        }

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
        // spendingList가 비어있지 않으면 imgWarningStart 숨기기
        val isEmpty = uiState.spendingList.isEmpty()

        // imgWarningStart visibility 설정
        binding.imgWarningStart.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun setupSwipeToDismiss(view: View) {
        var startY = 0f
        var isSwipingDown = false

        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startY = event.y // 시작 Y 좌표 저장
                    isSwipingDown = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaY = event.y - startY // Y 좌표 변화량 계산
                    if (deltaY > 100) { // 아래로 스와이프 거리 임계값
                        isSwipingDown = true
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isSwipingDown) {
                        // 아래로 스와이프 완료 시 애니메이션 추가
                        v.animate()
                            .translationY(v.height.toFloat()) // 화면 아래로 이동
                            .alpha(0f) // 투명도 0으로
                            .setDuration(300) // 300ms 애니메이션
                            .withEndAction {
                                v.visibility = View.GONE // 애니메이션 후 뷰 숨김
                            }
                            .start()
                    } else {
                        // 클릭 동작 처리
                        v.performClick()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun showRewardDialog() {
        val dialog = Dialog(requireContext())
        dialog.show()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        // 어두워지는 정도 설정 (0.0 ~ 1.0)
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
}
