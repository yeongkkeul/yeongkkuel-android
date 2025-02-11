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
import timber.log.Timber
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
        fun initBotSheet() {
            viewModel.getSpendingList()
        }

        fun initDate() {
            val currentDate = Date()
            val dateFormatChart = SimpleDateFormat("MM월 dd일 (E)", Locale.KOREAN)
            val formattedDateChart = dateFormatChart.format(currentDate)
            tvChartDate.text = formattedDateChart
        }

        fun initErrorListener() {
            fun hideErrorMessage() {
                ivError.animate()
                    .translationY(ivError.height.toFloat())  // 아래로 이동
                    .alpha(0f)  // 투명하게 변경
                    .setDuration(300)  // 300ms 동안 실행
                    .withEndAction {
                        ivError.visibility = View.GONE  // 애니메이션 종료 후 숨기기
                    }
                    .start()
            }
            ivError.setOnClickListener {
                hideErrorMessage()
            }
        }

        initBotSheet()
        initDate()
        initErrorListener()
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
            val spendingList = uiState.spendingList
            val totalList = spendingList.map { spending ->
                spending.history.sumOf { it.price }
            }

            val total = totalList.sum()
            val othersTotal = uiState.targetSpending - total

            // 목표 지출액 텍스트 설정
            tvChartTarget.text = total.toMoneyString() + "원"
            tvChartDescription.visibility = if (uiState.targetSpending < 0) View.GONE else View.VISIBLE
            tvChartDescription.text = if (othersTotal > 0) {
                "하루 목표 지출액보다\n${othersTotal.toMoneyString()}원 덜 썼어요!"
            } else {
                "하루 목표 지출액보다\n${(-othersTotal).toMoneyString()}원 더 썼어요!"
            }

            // PieEntry 리스트 생성
            val pieChartDataList = ArrayList<PieEntry>().apply {
                if (uiState.targetSpending >= 0) {
                    spendingList.forEach { spending ->
                        val totalPrice = spending.history.sumOf { it.price }
                        add(PieEntry(totalPrice.toFloat(), spending.kind))
                    }
                    if (othersTotal > 0) add(PieEntry(othersTotal.toFloat(), "나머지"))
                } else {
                    add(PieEntry(1f, "나머지"))
                }
            }

            // 색상 리스트 생성
            val colorList = spendingList.map {
                ContextCompat.getColor(requireContext(), it.color.id)
            }.toMutableList().apply {
                if (uiState.targetSpending < 0) clear()
                add(ContextCompat.getColor(requireContext(), R.color.black1))
            }

            // PieData 설정
            val pieData = PieData(PieDataSet(pieChartDataList, "").apply {
                colors = colorList
                valueTextSize = 16F
                setDrawValues(false) // 값 표시 비활성화
            })

            // PieChart 설정
            pieChart.apply {
                data = pieData
                description.isEnabled = false
                legend.isEnabled = false
                isRotationEnabled = true
                setDrawEntryLabels(false)
                setEntryLabelColor(Color.BLACK)
                animateY(1400, Easing.EaseInOutQuad)
                setTouchEnabled(false)
                setOnChartValueSelectedListener(null)
            }

            // JSON 변환 및 정렬
            val copiedList = Gson().fromJson<List<PieEntry>>(
                Gson().toJson(pieChartDataList), object : TypeToken<List<PieEntry>>() {}.type
            ).sortedByDescending { it.value }
        }


        fun showErrorMessage() {
            if (uiState.targetSpending == -1) {
                ivError.visibility = View.VISIBLE
                ivError.translationY = ivError.height.toFloat()  // 아래에서 시작
                ivError.alpha = 0f  // 투명도 0으로 시작

                ivError.animate()
                    .translationY(0f)  // 원래 위치로 이동
                    .alpha(1f)  // 투명도를 1로 변경
                    .setDuration(300)  // 300ms 동안 애니메이션 실행
                    .start()
            }
        }

        initPieChart()
        showErrorMessage()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}