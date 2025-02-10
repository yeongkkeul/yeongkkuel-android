package com.example.yeongkkuel.presentation.stat.monthly.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.ItemCalendarDayBinding
import com.example.yeongkkuel.databinding.ItemCalendarDayofweekBinding
import com.example.yeongkkuel.databinding.ItemUnknownBinding
import com.example.yeongkkuel.presentation.botsheet.BotSheetViewModel
import com.example.yeongkkuel.presentation.stat.monthly.StatMonthlyUiState
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet

class StatMonthlyCalendarListAdapter(
    private val viewModel: BotSheetViewModel
) : ListAdapter<StatMonthlyUiState.CalendarData, StatMonthlyCalendarListAdapter.ViewHolder>(
    StatMonthlyCalendarDiffUtil()
) {
    private var targetExpenditure: Int? = null

    fun setTargetExpenditure(targetExpenditure:Int?){
        this.targetExpenditure = targetExpenditure
    }

    abstract inner class ViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: StatMonthlyUiState.CalendarData)
    }

    enum class ViewType {
        DayOfWeeK, Day
    }

    inner class DayOfWeekViewHolder(
        private val binding: ItemCalendarDayofweekBinding
    ) : ViewHolder(binding.root) {
        override fun bind(item: StatMonthlyUiState.CalendarData)= with(binding) {
            (item as StatMonthlyUiState.CalendarData.CalendarDayOfWeek).let{
                tvDayOfWeek.text = it.dayOfWeek.kor
            }
        }
    }


    inner class DayViewHolder(
        private val binding: ItemCalendarDayBinding
    ) : ViewHolder(binding.root) {
        override fun bind(item: StatMonthlyUiState.CalendarData): Unit = with(binding) {
            (item as StatMonthlyUiState.CalendarData.CalendarDay).let { dayItem ->
                fun initData() {
                    tvDay.text = dayItem.day.toString()
                    if(targetExpenditure == null){
                        pieChart.visibility = View.INVISIBLE
                    } else {
                        val colorList = listOf(
                            ContextCompat.getColor(binding.root.context, R.color.main4),
                            ContextCompat.getColor(binding.root.context, R.color.black0),
                        )

                        val dataSet = PieDataSet(dayItem.pieDataList, "").apply {
                            colors = colorList // 색상 리스트 적용
                        }

                        dataSet.setDrawValues(false)

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
//                        animate()
                        }
                    }
                }

                if(dayItem.day == 0){
                    binding.root.visibility = View.INVISIBLE
                } else {
                    binding.root.visibility = View.VISIBLE
                    binding.root.setOnClickListener {
                        item.run {
                            viewModel.getSpendingList(
                                year = targetMonth.first,
                                month = targetMonth.second,
                                day = day
                            )
                        }
                    }
                    initData()
                }
            }
        }
    }

    inner class UnknownViewHolder(
        private val binding: ItemUnknownBinding
    ) : ViewHolder(binding.root) {
        override fun bind(item: StatMonthlyUiState.CalendarData) {
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is StatMonthlyUiState.CalendarData.CalendarDayOfWeek -> ViewType.DayOfWeeK.ordinal
        is StatMonthlyUiState.CalendarData.CalendarDay -> ViewType.Day.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        when (viewType) {
            ViewType.DayOfWeeK.ordinal -> DayOfWeekViewHolder(
                binding = ItemCalendarDayofweekBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            ViewType.Day.ordinal -> DayViewHolder(
                binding = ItemCalendarDayBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            else -> UnknownViewHolder(
                binding = ItemUnknownBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}

private class StatMonthlyCalendarDiffUtil :
    DiffUtil.ItemCallback<StatMonthlyUiState.CalendarData>() {
    override fun areItemsTheSame(
        oldItem: StatMonthlyUiState.CalendarData,
        newItem: StatMonthlyUiState.CalendarData
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: StatMonthlyUiState.CalendarData,
        newItem: StatMonthlyUiState.CalendarData
    ): Boolean {
        return oldItem == newItem
    }
}