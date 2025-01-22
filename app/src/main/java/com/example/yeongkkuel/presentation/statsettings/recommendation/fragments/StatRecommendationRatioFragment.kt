package com.example.yeongkkuel.presentation.statsettings.recommendation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.yeongkkuel.databinding.ItemStatRecommendationRatioBinding
import com.example.yeongkkuel.presentation.statsettings.StatSettingsViewModel
import com.example.yeongkkuel.presentation.util.setUnderlineBehavior
import com.example.yeongkkuel.presentation.util.toMoneyString

class StatRecommendationRatioFragment(
    private val viewModel: StatSettingsViewModel
) : Fragment() {
    private var _binding: ItemStatRecommendationRatioBinding? = null
    private val binding: ItemStatRecommendationRatioBinding
        get() = requireNotNull(_binding) { "ItemStatRecommendationRatioBinding -> null" }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ItemStatRecommendationRatioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() = with(binding){
        fun initEtListener(){
            etRatio.run{
                toMoneyString()
                setUnderlineBehavior()
            }
        }
        initEtListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}