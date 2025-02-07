package com.example.yeongkkuel.presentation.stat.daily

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentStatDailyBinding
import com.example.yeongkkuel.presentation.botsheet.BotSheetUiState
import com.example.yeongkkuel.presentation.botsheet.BotSheetViewModel
import com.example.yeongkkuel.presentation.util.toMoneyString
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatDailyFragment : Fragment() {
    private var _binding: FragmentStatDailyBinding? = null
    private val binding: FragmentStatDailyBinding
        get() = requireNotNull(_binding) { "FragmentStatBinding -> null" }

    private val viewModel: BotSheetViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStatDailyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initViewModel()
    }

    private fun initView() = with(binding) {
        fun initBotSheet(){
            viewModel.getSpendingList()
        }

        fun initDate() {
            val currentDate = Date()
            val dateFormatChart = SimpleDateFormat("MM월 dd일 (E)", Locale.KOREAN)
            val formattedDateChart = dateFormatChart.format(currentDate)
            tvChartDate.text = formattedDateChart
        }

        initBotSheet()
        initDate()
    }

    private fun initViewModel() = with(viewModel) {
        viewLifecycleOwner.lifecycleScope.launch {
            uiState.flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collectLatest { uiState ->
                    onBind(uiState)
                }
        }
    }

    private fun onBind(uiState: BotSheetUiState) = with(binding) {
        fun initPieChart() {
            uiState.spendingList.let {
                val totalList = uiState.spendingList.map { spending ->
                    spending.history.sumOf { history -> history.price }
                }

                val othersTotal = uiState.targetSpending - totalList.sum()

                val otherTotalString = Math.abs(othersTotal).toMoneyString() + "원"

                if(uiState.targetSpending < 0){
                    tvChartDescription.visibility = View.GONE
                    tvChartTarget.text = "하루 목표 지출액을\n" +
                            "설정해주세요."
                }
                else {
                    tvChartTarget.text = otherTotalString
                    tvChartDescription.visibility = View.VISIBLE
                    if (othersTotal > 0) {
                        tvChartDescription.text = "하루 목표 지출액보다\n" +
                                "${otherTotalString}원 덜 썻어요!"
                    } else {
                        tvChartDescription.text = "하루 목표 지출액보다\n" +
                                "${otherTotalString}원 더 썻어요!"
                    }
                }

                val pieChartDataList = ArrayList<PieEntry>().apply {
                    uiState.spendingList.forEach { spending ->
                        val totalPrice = spending.history.sumOf { it.price }
                        add(PieEntry(totalPrice.toFloat(), spending.kind))
                    }
                    add(PieEntry(othersTotal.toFloat(), "나머지"))
                }

                val colorList = uiState.spendingList.map {
                    ContextCompat.getColor(requireContext(), it.color.id)
                }.toMutableList()

                colorList.add(ContextCompat.getColor(requireContext(), R.color.black1)) // 색상 추가

                val dataSet = PieDataSet(pieChartDataList, "").apply {
                    colors = colorList // 색상 리스트 적용
                }

                dataSet.valueTextSize = 16F
                dataSet.setDrawValues(false) // value 비활성화

                val pieData = PieData(dataSet)

                pieChart.apply {
                    data = pieData
                    description.isEnabled = false // 차트 설명 비활성화
                    legend.isEnabled = false // 하단 설명 비활성화
                    isRotationEnabled = true // 차트 회전 활성화
                    setDrawEntryLabels(false) // 엔트리 라벨 비활성화
                    setEntryLabelColor(Color.BLACK) // label 색상
                    animateY(1400, Easing.EaseInOutQuad) // 1.4초 동안 애니메이션 설정
                    setTouchEnabled(false)  // 차트 터치 비활성화
                    setOnChartValueSelectedListener(null)  // 클릭 이벤트 리스너 제거
                    animate()
                }

                // Gson 객체 생성
                val gson = Gson()

                // 원본 데이터를 JSON 형식으로 직렬화
                val jsonString = gson.toJson(pieChartDataList)

                // JSON 형식의 데이터를 다시 역직렬화하여 리스트로 변환
                val typeToken = object : TypeToken<List<PieEntry>>() {}.type
                val copiedList = gson.fromJson<List<PieEntry>>(jsonString, typeToken)

                // 내림차순으로 정렬
                val sortedList = copiedList.sortedByDescending { it.value }
            }
        }

        initPieChart()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}