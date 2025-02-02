package com.example.yeongkkuel.presentation.stat.monthly

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.yeongkkuel.databinding.FragmentStatMonthlyBinding
import com.example.yeongkkuel.presentation.botsheet.BotSheetViewModel
import com.example.yeongkkuel.presentation.stat.monthly.adapter.viewpager.StatMonthlyCalenderViewPagerAdapter
import com.example.yeongkkuel.presentation.util.toMoneyString
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StatMonthlyFragment(
    private val viewModel: StatMonthlyViewModel
) : Fragment() {
    private var _binding: FragmentStatMonthlyBinding? = null
    private val binding: FragmentStatMonthlyBinding
        get() = requireNotNull(_binding) { "FragmentStatMonthlyBinding -> null" }


    private val botViewModel: BotSheetViewModel by activityViewModels()

    private val calendarViewPagerAdapter by lazy {
        StatMonthlyCalenderViewPagerAdapter(requireActivity(), viewModel = viewModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStatMonthlyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initViewModel()
    }

    private fun initView() = with(binding) {
        fun initVp() {
            // ViewPager2 설정
            vpCalendar.run {
                offscreenPageLimit = 3
                adapter = calendarViewPagerAdapter
                setCurrentItem(adapter?.itemCount?.minus(1) ?: 0, false)
            }
        }

        initVp()
    }



    private fun initViewModel() = with(viewModel) {
        viewLifecycleOwner.lifecycleScope.launch {
            uiState.flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collectLatest { uiState ->
                    onBind(uiState)
                }
        }
    }

    private fun onBind(uiState: StatMonthlyUiState)= with(binding){
        tvCurrentMonth.text = uiState.targetMonth.first.toString() + "년 " + uiState.targetMonth.second.toString() + "월"

        tvTotalSpending.text = uiState.totalSpending.toMoneyString() + "원"

        tvAchievementDay.text = uiState.achieveDay.toString() + "일"
        tvRewardAmount.text = "+" + uiState.rewardsAmount.toString()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}