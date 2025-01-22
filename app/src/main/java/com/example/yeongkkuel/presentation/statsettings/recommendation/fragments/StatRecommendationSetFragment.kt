package com.example.yeongkkuel.presentation.statsettings.recommendation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.yeongkkuel.databinding.ItemStatRecommendationRatioBinding
import com.example.yeongkkuel.databinding.ItemStatRecommendationSettingBinding
import com.example.yeongkkuel.presentation.util.setUnderlineBehavior
import com.example.yeongkkuel.presentation.util.toMoneyString

class StatRecommendationSetFragment : Fragment() {
    private var _binding: ItemStatRecommendationSettingBinding? = null
    private val binding: ItemStatRecommendationSettingBinding
        get() = requireNotNull(_binding) { "ItemStatRecommendationSettingBinding -> null" }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ItemStatRecommendationSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() = with(binding) {
        fun initEtListener() {
            etSet.run {
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