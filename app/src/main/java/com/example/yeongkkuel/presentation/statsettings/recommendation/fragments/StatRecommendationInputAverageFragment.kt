package com.example.yeongkkuel.presentation.statsettings.recommendation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.yeongkkuel.databinding.ItemStatRecommendationInputAverageBinding
import com.example.yeongkkuel.presentation.util.setUnderlineBehavior
import com.example.yeongkkuel.presentation.util.toMoneyString

class StatRecommendationInputAverageFragment : Fragment() {
    private var _binding: ItemStatRecommendationInputAverageBinding? = null
    private val binding: ItemStatRecommendationInputAverageBinding
        get() = requireNotNull(_binding) { "ItemStatRecommendationInputAverageBinding -> null" }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ItemStatRecommendationInputAverageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() = with(binding){
        fun initEtListener(){
            etOutcome.run{
                toMoneyString()
                setUnderlineBehavior()
            }
            etSpending.run{
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