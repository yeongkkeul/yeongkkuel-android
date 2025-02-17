package com.example.yeongkkuel.presentation.stat

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.yeongkkuel.R
import com.example.yeongkkuel.presentation.stat.daily.StatDailyFragment
import com.example.yeongkkuel.presentation.stat.monthly.StatMonthlyFragment
import com.example.yeongkkuel.presentation.stat.monthly.StatMonthlyViewModel
import com.example.yeongkkuel.presentation.stat.weekly.StatWeeklyFragment
import com.example.yeongkkuel.presentation.stat.weekly.StatWeeklyViewModel

class StatViewPagerAdapter(
    fragment: Fragment,
    weeklyViewModel: StatWeeklyViewModel,
    monthlyViewModel: StatMonthlyViewModel
) : FragmentStateAdapter(fragment){

    private val fragments = listOf(
        StatTapModel(StatDailyFragment(), R.string.stat_daily),
        StatTapModel(StatWeeklyFragment(weeklyViewModel), R.string.stat_weekly),
        StatTapModel(StatMonthlyFragment(monthlyViewModel), R.string.stat_monthly)
    )

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position].fragment
    }

    fun getTitle(position: Int): Int = fragments[position].title

    fun animate(position: Int){
        (fragments[position]?.fragment as? StatAnimationListener)?.animate()
    }


}