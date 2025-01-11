package com.example.yeongkkuel.presentation.stat.weekly

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.yeongkkuel.R
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

    private val weekListAdapter by lazy {
        StatWeeklyWeekListAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStatWeeklyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initViewModel()
    }

    private fun initView() = with(binding) {
        fun initRv() {
            rvWeekSending.run {
                adapter = weekListAdapter
                layoutManager = GridLayoutManager(requireContext(), 7)
            }
        }

        initRv()
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
        uiState.weekList.let { list ->
            weekListAdapter.submitList(list)

            val entries = list.mapNotNull {
                it.entry
            }
            entries.forEachIndexed { index, entry ->
                val iconRes = if (entry.y > uiState.targetSpending) {
                    R.drawable.ic_hamberger  // 적절한 리소스 이름으로 변경
                } else {
                    R.drawable.ic_bell  // 적절한 리소스 이름으로 변경
                }
                val drawable = ContextCompat.getDrawable(requireContext(), iconRes)

                if (drawable != null) {
                    entry.icon = drawable
                }
            }

            // LineDataSet 생성
            val dataSet = LineDataSet(entries, "Label").apply {
                // 선의 색, 두께 설정
                color = ContextCompat.getColor(requireContext(), R.color.black1)
                lineWidth = 1f

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

                setTouchEnabled(false)
                isDragEnabled = false  // 드래그 비활성화
                isScaleXEnabled = false // X축 스케일링 비활성화
                isScaleYEnabled = false // Y축 스케일링 비활성화

                xAxis.run {
                    setDrawGridLines(false)
                    setDrawAxisLine(false)
                    axisLineColor = Color.TRANSPARENT
                    textColor = Color.TRANSPARENT

                    granularity = 1f
                    axisMinimum = 0f
                    axisMaximum = 6f
                }
                val maxValue = entries.maxOf { it.y }
                val minValue = entries.minOf { it.y }

                val maxDiff = maxValue - uiState.targetSpending
                val minDiff = uiState.targetSpending - minValue

                axisLeft.run {
                    val diff = if (minDiff > maxDiff) minDiff else maxDiff

                    // y축의 최소값과 최대값을 targetSpending을 기준으로 설정
                    axisMinimum = uiState.targetSpending - diff
                    axisMaximum = uiState.targetSpending + diff

                    setDrawGridLines(true)  // 그리드선 표시
                    setDrawAxisLine(true)   // 축선 그리기
                    axisLineColor = Color.BLACK
                    textColor = Color.BLACK
                }
                axisLeft.apply {
                    setDrawGridLines(false)
                    setDrawAxisLine(false)
                    axisLineColor = Color.TRANSPARENT
                    textColor = Color.TRANSPARENT
                }
                axisRight.apply {
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