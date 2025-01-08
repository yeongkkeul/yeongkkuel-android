package com.example.yeongkkuel.presentation.stat.weekly

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.yeongkkuel.databinding.FragmentStatWeeklyBinding
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StatWeeklyFragment : Fragment() {
    private var _binding: FragmentStatWeeklyBinding? = null
    private val binding: FragmentStatWeeklyBinding
        get() = requireNotNull(_binding) { "FragmentStatWeeklyBinding -> null" }

    private val viewModel: StatWeeklyViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStatWeeklyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViewModel()
    }

    private fun initViewModel() = with(viewModel) {
        viewLifecycleOwner.lifecycleScope.launch {
            uiState.flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collectLatest { uiState ->
                    onBind(uiState)
                }
        }
    }

    private fun onBind(uiState: StatWeeklyUiState) = with(binding) {
        uiState.charEntryList.let { entries ->
            val icons = mutableListOf<Drawable>()

            // LineDataSet 생성
            val dataSet = LineDataSet(entries, "Label").apply {
                // 선의 색, 두께 설정
                color = Color.GRAY
                lineWidth = 2f

                setDrawValues(false)
                setDrawIcons(true)
            }

            val lineData = LineData(dataSet)

            lineChart.run {
                data = lineData
                setBackgroundColor(Color.WHITE)
                invalidate()  // 차트 새로 그리기

                description.isEnabled = false
                legend.isEnabled = false

                xAxis.run {
                    setDrawGridLines(false)
                    setDrawAxisLine(false)
                    axisLineColor = Color.TRANSPARENT
                    textColor = Color.TRANSPARENT

                    granularity = 1f
                    axisMinimum = 0f
                    axisMaximum = 6f
                }
                axisLeft.apply {
                    setDrawGridLines(false)
                    setDrawAxisLine(false)
                    axisLineColor = Color.TRANSPARENT
                    textColor = Color.TRANSPARENT
                }
                axisRight.apply{
                    setDrawGridLines(false)
                    setDrawAxisLine(false)
                    axisLineColor = Color.TRANSPARENT
                    textColor = Color.TRANSPARENT
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}