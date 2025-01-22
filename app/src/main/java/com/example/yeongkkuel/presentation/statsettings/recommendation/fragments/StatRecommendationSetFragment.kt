package com.example.yeongkkuel.presentation.statsettings.recommendation.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.ItemStatRecommendationSettingBinding
import com.example.yeongkkuel.presentation.statsettings.RecommendStep
import com.example.yeongkkuel.presentation.statsettings.StatSettingsUiState
import com.example.yeongkkuel.presentation.statsettings.StatSettingsViewModel
import com.example.yeongkkuel.presentation.util.setLimit
import com.example.yeongkkuel.presentation.util.setUnderlineBehavior
import com.example.yeongkkuel.presentation.util.toMoneyString
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StatRecommendationSetFragment(
    private val viewModel: StatSettingsViewModel
) : Fragment() {
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
        initViewModel()
    }

    private fun initView() = with(binding) {
        fun initEtListener() {
            etSet.run {
                toMoneyString()
                setUnderlineBehavior(tvSetError)
                setLimit(Int.MAX_VALUE)
            }
        }
        initEtListener()
    }

    private fun initViewModel() = with(viewModel){
        viewLifecycleOwner.lifecycleScope.launch {
            uiState.flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collectLatest { uiState ->
                    onBind(uiState)
                }
        }
    }

    private fun onBind(uiState: StatSettingsUiState) = with(binding) {
        val recommendSpendingText = "추천 드릴 하루 목표 지출액은\n" +
                "${uiState.recommendSpending!!.toMoneyString()}원 입니다."

        val spannable = SpannableString(recommendSpendingText)
        val start = recommendSpendingText.indexOf(uiState.recommendSpending.toMoneyString())
        val end = start + uiState.recommendSpending.toMoneyString().length

        if (start >= 0) {
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.sub1)), // 텍스트 색상
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                StyleSpan(Typeface.BOLD), // 텍스트 스타일 (볼드)
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        tvRecommendSpending.text = spannable

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}