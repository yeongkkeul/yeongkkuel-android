package com.example.yeongkkuel.presentation.statsettings.recommendation.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.yeongkkuel.presentation.statsettings.recommendation.fragments.StatRecommendationInputoutcomeFragment
import com.example.yeongkkuel.presentation.statsettings.recommendation.fragments.StatRecommendationRatioFragment
import com.example.yeongkkuel.presentation.statsettings.recommendation.fragments.StatRecommendationSetFragment

class StatRecommendationViewPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment){

    private val fragments = listOf<Fragment>(
        StatRecommendationInputoutcomeFragment(),
        StatRecommendationRatioFragment(),
        StatRecommendationSetFragment()
    )

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

}