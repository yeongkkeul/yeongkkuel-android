package com.example.yeongkkuel.presentation.statsettings.recommendation

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.yeongkkuel.presentation.stat.weekly.StatWeeklyFragment

class StatRecommendationViewPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment){

    private val fragments = listOf<Fragment>(
        StatWeeklyFragment()
    )

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

}