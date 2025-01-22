package com.example.yeongkkuel.presentation.statsettings.recommendation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.viewpager2.widget.ViewPager2
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentStatRecommendationBinding
import com.example.yeongkkuel.presentation.statsettings.StatSettingsViewModel
import com.example.yeongkkuel.presentation.statsettings.recommendation.adapter.StatRecommendationViewPagerAdapter

class StatRecommendationFragment : Fragment() {
    private var _binding: FragmentStatRecommendationBinding? = null
    private val binding: FragmentStatRecommendationBinding
        get() = requireNotNull(_binding) { "FragmentStatRecommendationBinding -> null" }

    private val viewModel: StatSettingsViewModel by viewModels()


    private val viewPagerAdapter by lazy {
        StatRecommendationViewPagerAdapter(
            fragment = this@StatRecommendationFragment,
            viewModel = viewModel)
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

            isUserInputEnabled = false

            progressBar.max = 3

            // 페이지 변경 시 ProgressBar 업데이트
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                    progressBar.progress = position + 1
                }

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                }
            })

            btnBlue.setOnClickListener {
                if (currentItem < viewPagerAdapter.itemCount - 1) {
                    currentItem++
                }
            }

            btnGray.setOnClickListener {
                if (currentItem > 0) {
                    currentItem--
                }
            }
        }

        fun initBack() {
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
    }
}