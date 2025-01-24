package com.example.yeongkkuel.presentation.statsettings.recommendation.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.yeongkkuel.presentation.statsettings.StatSettingsViewModel
import com.example.yeongkkuel.presentation.statsettings.recommendation.fragments.StatRecommendationInputAverageFragment
import com.example.yeongkkuel.presentation.statsettings.recommendation.fragments.StatRecommendationRatioFragment
import com.example.yeongkkuel.presentation.statsettings.recommendation.fragments.StatRecommendationSetFragment

class StatRecommendationViewPagerAdapter(
    fragment: Fragment,
    private val viewModel: StatSettingsViewModel
) : FragmentStateAdapter(fragment){

    private val fragments = listOf(
        StatRecommendationInputAverageFragment(viewModel),
        StatRecommendationRatioFragment(viewModel),
        StatRecommendationSetFragment(viewModel)
    )

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    fun setAverage() {
        (fragments[0] as StatRecommendationInputAverageFragment).setAverage()
    }

    fun setRatio(){
        (fragments[1] as StatRecommendationRatioFragment).setRatio()
    }

    fun setTargetSpending(){
        (fragments[2] as StatRecommendationSetFragment).setTargetSpending()
    }

}