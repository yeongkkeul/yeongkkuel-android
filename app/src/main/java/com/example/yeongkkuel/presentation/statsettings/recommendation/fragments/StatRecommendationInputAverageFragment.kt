package com.example.yeongkkuel.presentation.statsettings.recommendation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.yeongkkuel.databinding.ItemStatRecommendationInputAverageBinding
import com.example.yeongkkuel.presentation.statsettings.StatSettingsViewModel
import com.example.yeongkkuel.presentation.util.clearComma
import com.example.yeongkkuel.presentation.util.setLimit
import com.example.yeongkkuel.presentation.util.setUnderlineBehavior
import com.example.yeongkkuel.presentation.util.toEditable
import com.example.yeongkkuel.presentation.util.toMoneyString
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StatRecommendationInputAverageFragment(
    private val viewModel: StatSettingsViewModel
) : Fragment() {
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
        initViewModel()
    }

    private fun initView() = with(binding) {
        fun initEtListener() {
            etOutcome.run {
                toMoneyString()
                setUnderlineBehavior(tvOutcomeError)
                setLimit(Int.MAX_VALUE)
            }
            etIncome.run {
                toMoneyString()
                setUnderlineBehavior(tvIncomeError)
                setLimit(Int.MAX_VALUE)
            }
        }
        initEtListener()
    }

    fun setAverage() {
        binding.run {
            val income = etIncome.text.toString().clearComma()
            val outcome = etOutcome.text.toString().clearComma()

            tvIncomeError.visibility = if (income == null) View.VISIBLE else View.GONE
            tvOutcomeError.visibility = if (outcome == null) View.VISIBLE else View.GONE

            if (income != null && outcome != null) {
                viewModel.setAverage(income = income, outcome = outcome)
            }
        }
    }

    private fun initViewModel() = with(viewModel){
        viewLifecycleOwner.lifecycleScope.launch {
            uiState.flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collectLatest { uiState ->
                    binding.etOutcome.text = uiState.averageOutcome?.toMoneyString()?.toEditable()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}