package com.example.yeongkkuel.presentation.statsettings.recommendation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.viewpager2.widget.ViewPager2
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentStatRecommendationBinding

class StatRecommendationFragment : Fragment() {
    private var _binding: FragmentStatRecommendationBinding? = null
    private val binding: FragmentStatRecommendationBinding
        get() = requireNotNull(_binding) { "FragmentStatRecommendationBinding -> null" }

    private val viewPagerAdapter by lazy {
        StatRecommendationViewPagerAdapter(this@StatRecommendationFragment)
    }

    val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val navOptions = navOptions {
                popUpTo(R.id.navigation_stat) {
                    inclusive = false // 특정 프래그먼트를 제외하고 그 위의 프래그먼트들을 pop
                }
                launchSingleTop = true // 새로운 목적지로 이동할 때 중복되지 않도록 설정
            }

            findNavController().navigate(R.id.navigation_stat, null, navOptions)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStatRecommendationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    private fun initView() = with(binding) {
        fun initVp() = with(vpRecommendation) {
            adapter = viewPagerAdapter

            progressBar.max = viewPagerAdapter.itemCount - 1

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                    // 현재 페이지와 오프셋을 기반으로 진행 상태 설정
                    val progress = position + positionOffset
                    progressBar.progress =
                        (progress * 100 / (viewPagerAdapter.itemCount - 1)).toInt()
                }

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    // 선택된 페이지에 대한 추가 처리 필요 시
                }
            })
        }

        fun initBack() {
            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                backPressedCallback
            )

            ivTopArrow.setOnClickListener {
                activity?.onBackPressed()
            }
        }

        initVp()
        initBack()

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        backPressedCallback.remove()
    }
}