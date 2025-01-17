package com.example.yeongkkuel.presentation.stat.monthly.adapter.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.yeongkkuel.presentation.stat.monthly.StatMonthlyViewModel

class StatMonthlyCalenderViewPagerAdapter(
    fragmentActivity: FragmentActivity,
    viewModel: StatMonthlyViewModel
) : FragmentStateAdapter(fragmentActivity) {

    private val fragmentList = listOf(
        StatMonthlyCalendarFrameFragment(viewModel, 12),
        StatMonthlyCalendarFrameFragment(viewModel, 11),
        StatMonthlyCalendarFrameFragment(viewModel, 10),
        StatMonthlyCalendarFrameFragment(viewModel, 9),
        StatMonthlyCalendarFrameFragment(viewModel, 8),
        StatMonthlyCalendarFrameFragment(viewModel, 7),
        StatMonthlyCalendarFrameFragment(viewModel, 6),
        StatMonthlyCalendarFrameFragment(viewModel, 5),
        StatMonthlyCalendarFrameFragment(viewModel, 4),
        StatMonthlyCalendarFrameFragment(viewModel, 3),
        StatMonthlyCalendarFrameFragment(viewModel, 2),
        StatMonthlyCalendarFrameFragment(viewModel, 1),
        StatMonthlyCalendarFrameFragment(viewModel, 0)
    )

    override fun getItemCount(): Int {
        return fragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }
}
