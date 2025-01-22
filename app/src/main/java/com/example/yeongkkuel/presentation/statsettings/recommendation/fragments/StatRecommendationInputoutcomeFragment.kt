package com.example.yeongkkuel.presentation.statsettings.recommendation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.yeongkkuel.databinding.ItemStatRecommendationInputoutcomeBinding

class StatRecommendationInputoutcomeFragment: Fragment() {
    private var _binding: ItemStatRecommendationInputoutcomeBinding? = null
    private val binding: ItemStatRecommendationInputoutcomeBinding
        get() = requireNotNull(_binding) { "ItemStatRecommendationInputoutcomeBinding -> null" }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ItemStatRecommendationInputoutcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}