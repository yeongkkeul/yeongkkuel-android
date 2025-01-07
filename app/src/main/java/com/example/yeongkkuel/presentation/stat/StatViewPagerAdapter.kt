package com.example.yeongkkuel.presentation.stat

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.yeongkkuel.R
import com.example.yeongkkuel.presentation.stat.daily.StatDailyFragment
import com.example.yeongkkuel.presentation.stat.monthly.StatMonthlyFragment
import com.example.yeongkkuel.presentation.stat.weekly.StatWeeklyFragment

class StatViewPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment){

    private val fragments = listOf(
        StatTapModel(StatDailyFragment(), R.string.stat_daily),
        StatTapModel(StatWeeklyFragment(), R.string.stat_weekly),
        StatTapModel(StatMonthlyFragment(), R.string.stat_monthly)
    )

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position].fragment
    }

    fun getTitle(position: Int): Int = fragments[position].title
}