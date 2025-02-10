package com.example.yeongkkuel.presentation.stat.weekly

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentStatWeeklyBinding
import com.example.yeongkkuel.presentation.stat.weekly.adapter.StatWeeklyCompareListAdapter
import com.example.yeongkkuel.presentation.stat.weekly.adapter.StatWeeklyPieChartCategoryListAdapter
import com.example.yeongkkuel.presentation.stat.weekly.adapter.StatWeeklyWeekListAdapter
import com.example.yeongkkuel.presentation.util.Week
import com.example.yeongkkuel.presentation.util.toMoneyString
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale
import kotlin.math.exp

class StatWeeklyFragment(
    private val viewModel: StatWeeklyViewModel
) : Fragment() {
    private var _binding: FragmentStatWeeklyBinding? = null
    private val binding: FragmentStatWeeklyBinding
        get() = requireNotNull(_binding) { "FragmentStatWeeklyBinding -> null" }

    private val weekListAdapter by lazy {
        StatWeeklyWeekListAdapter()
    }

    private val compareListAdapter by lazy {
        StatWeeklyCompareListAdapter()
    }

    private val pieChartCategoryListAdapter by lazy {
        StatWeeklyPieChartCategoryListAdapter()
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

        getData()
        initView()
        initViewModel()
    }

    private fun getData() {
        viewModel.getWeekExpenditureList()
        viewModel.getWeekExpenditureAverage()
    }

    private fun initView() = with(binding) {
        fun initRv() {
            rvWeekSending.run {
                adapter = weekListAdapter
                layoutManager = GridLayoutManager(requireContext(), 7)
            }

            rvCompare.run {
                adapter = compareListAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }

            rvSpendingCategory.run {
                adapter = pieChartCategoryListAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
        }

        fun initCurrentWeek() {
            val calendar = Calendar.getInstance()

            // 오늘 날짜를 기준으로 계산
            val currentWeekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH)

            // 해당 주가 몇 번째 주인지 표시
            val weekText = "${calendar.get(Calendar.MONTH) + 1}월 ${currentWeekOfMonth}주"

            // 결과를 TextView에 설정
            tvCurrentWeek.text = weekText
        }

        initRv()
        initCurrentWeek()
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
        fun initRvData() {
            weekListAdapter.submitList(uiState.weekList)
            compareListAdapter.submitList(uiState.compareList)
            pieChartCategoryListAdapter.submitList(uiState.pieChartList)
        }

        fun initLineChart() {
            uiState.weekList.let { list ->
                val todayWeekNum = getDayOfWeekNum(LocalDate.now())  // 오늘의 요일을 한글로 가져옴

                val entries = list.mapNotNull {
                    it.entry
                }.take(todayWeekNum)

                entries.forEachIndexed { index, entry ->
                    val iconRes = if (entry.y >= uiState.targetSpending) {
                        R.drawable.ic_point_up
                    } else {
                        R.drawable.ic_point_down
                    }

                    val drawable = ContextCompat.getDrawable(requireContext(), iconRes)
                    drawable?.let { entry.setIcon(it) }
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

        fun setTargetSpending() {
            weekListAdapter.setTargetSpending(uiState.targetSpending)

            tvTotalSpending.text = "${uiState.totalSpending.toMoneyString()}원"
            tvLineTargetSpending.text = "하루 목표 지출액 ${uiState.targetSpending.toMoneyString()}원"
        }

        fun initPieChart() {
            uiState.pieChartList.let { pieChartList ->
                val list = pieChartList.sortedByDescending { it.expenditure }
                val pieChartDataList = ArrayList<PieEntry>().apply {
                    if (list.all { it.expenditure == 0 }) {  // 모든 expenditure가 0이면
                        list.forEach { spending ->
                            add(PieEntry(1.0f, spending.category))  // 모든 값을 1로 변경
                        }
                    } else {
                        list.forEach { spending ->
                            add(PieEntry(spending.expenditure.toFloat(), spending.category))
                        }
                    }
                }

                val colorList = list.map {
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

        fun initPieChartDescription() {
            val mostSpendingKind = uiState.pieChartList
                .maxByOrNull { spending ->
                    spending.expenditure
                }?.category

            if (mostSpendingKind != null) {
                val mostSpendingKindKor = mostSpendingKind.name
                val message = "${mostSpendingKindKor}에 가장 많이 썼어요"

                if (mostSpendingKindKor == "")
                    tvMostSpending.text = "지출을 입력해주세요."
                else {
                    val spannable = SpannableString(message)

                    val start = message.indexOf(mostSpendingKindKor)
                    val end = start + mostSpendingKindKor.length
                    spannable.setSpan(
                        StyleSpan(Typeface.BOLD),
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    tvMostSpending.text = spannable
                }
            } else {
                tvMostSpending.text = "지출을 입력해주세요."
            }
        }

        initRvData()
        initLineChart()
        setTargetSpending()

        initPieChart()
        initPieChartDescription()
    }

    private fun getDayOfWeekNum(date: LocalDate): Int {
        val dayString = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN) // 예: "월요일"
        return when (dayString) {
            "월요일" -> 1
            "화요일" -> 2
            "수요일" -> 3
            "목요일" -> 4
            "금요일" -> 5
            "토요일" -> 6
            "일요일" -> 7
            else -> 0
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}