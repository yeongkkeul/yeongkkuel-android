package com.example.yeongkkuel.presentation.statsettings.recommendation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.yeongkkuel.databinding.FragmentStatRecommendationBinding
import com.example.yeongkkuel.presentation.statsettings.RecommendStep
import com.example.yeongkkuel.presentation.statsettings.StatSettingsUiState
import com.example.yeongkkuel.presentation.statsettings.StatSettingsViewModel
import com.example.yeongkkuel.presentation.statsettings.recommendation.adapter.StatRecommendationViewPagerAdapter
import com.example.yeongkkuel.presentation.util.toNaviStat
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StatRecommendationFragment : Fragment() {
    private var _binding: FragmentStatRecommendationBinding? = null
    private val binding: FragmentStatRecommendationBinding
        get() = requireNotNull(_binding) { "FragmentStatRecommendationBinding -> null" }

    private val viewModel: StatSettingsViewModel by viewModels()


    private val viewPagerAdapter by lazy {
        StatRecommendationViewPagerAdapter(
            fragment = this@StatRecommendationFragment,
            viewModel = viewModel
        )
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
        initViewModel()
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
                    when (position) {
                        2 -> {
                            btnBlue.text = "저장"
                        }

                        else -> {
                            btnBlue.text = "다음"
                        }
                    }

                }
            })

            btnBlue.setOnClickListener {
                when (currentItem) {
                    0 -> {
                        viewPagerAdapter.setAverage()
                    }

                    1 -> {
                        viewPagerAdapter.setRatio()
                    }

                    2 -> {}
                }
            }

            btnGray.setOnClickListener {
                if (currentItem > 0) {
                    currentItem--
                } else if (currentItem == 0) {
                    findNavController().toNaviStat()
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

    private fun initViewModel() = with(viewModel) {
        viewLifecycleOwner.lifecycleScope.launch {
            uiState.flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collectLatest { uiState ->
                    onBind(uiState)
                }
        }
    }

    private fun onBind(uiState: StatSettingsUiState) = with(binding) {
        when (uiState.recommendStep) {
            RecommendStep.AVERAGE -> vpRecommendation.currentItem = 0
            RecommendStep.RATIO -> vpRecommendation.currentItem = 1
            RecommendStep.SET -> vpRecommendation.currentItem = 2
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}