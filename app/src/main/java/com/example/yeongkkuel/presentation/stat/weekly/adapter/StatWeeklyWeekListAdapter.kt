package com.example.yeongkkuel.presentation.stat.weekly.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.ItemStatWeeklyWeekBinding
import com.example.yeongkkuel.presentation.stat.weekly.StatWeeklyUiState
import com.example.yeongkkuel.presentation.util.toMoneyString
import java.math.RoundingMode
import java.util.Calendar

class StatWeeklyWeekListAdapter(
) : ListAdapter<StatWeeklyUiState.DayData, StatWeeklyWeekListAdapter.ViewHolder>(
    StatWeeklyWeekDiffUtil()
) {
    private var targetSpending: Int? = null

    fun setTargetSpending(targetSpending: Int) {
        this.targetSpending = targetSpending
    }

    inner class ViewHolder(
        private val binding: ItemStatWeeklyWeekBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: StatWeeklyUiState.DayData) = with(binding) {
            tvDayOfWeek.text = item.dayOfWeek.kor

            // 요일에 맞춘 날짜를 계산하여 표시
            val today = Calendar.getInstance()
            val dayOfWeekIndex = item.dayOfWeek.ordinal + 1  // Calendar 요일은 1~7 (월~일)
            today.set(Calendar.DAY_OF_WEEK, dayOfWeekIndex)

            val dayOfMonth = today.get(Calendar.DAY_OF_MONTH)
            tvDay.text = dayOfMonth.toMoneyString()

            // 지출 금액을 백의자리 올림으로 n.n만 형식으로 표시
            if (item.entry != null) {
                val spendingInThousand = (item.entry.y / 10000.0).toBigDecimal()
                    .setScale(1, RoundingMode.UP)  // 소수 첫째 자리까지 반올림
                tvSpending.text = "${spendingInThousand}만"
            } else {
                tvSpending.text = "-"
            }


            // 오늘의 날짜인지 확인
            val currentDay = Calendar.getInstance()
            if (today.get(Calendar.YEAR) == currentDay.get(Calendar.YEAR) &&
                today.get(Calendar.MONTH) == currentDay.get(Calendar.MONTH) &&
                today.get(Calendar.DAY_OF_MONTH) == currentDay.get(Calendar.DAY_OF_MONTH)
            ) {
                ivTodayPoint.visibility = View.VISIBLE
            }

            // 오늘 요일보다 앞의 요일을 처리하는 조건 추가
            var todayWeek = currentDay.get(Calendar.DAY_OF_WEEK)  // 오늘의 요일 (1 = 일요일, 2 = 월요일, ...)
            var targetWeek = today.get(Calendar.DAY_OF_WEEK)  // 타겟 요일 (1 = 일요일, 2 = 월요일, ...)

            if(todayWeek == 1) todayWeek = 8
            if(targetWeek == 1) targetWeek = 8

            if (targetWeek > todayWeek) {
                tvSpending.text = "-"
            }


            targetSpending?.let { target ->
                item.entry?.let { entry ->
                    if (target.toFloat() <= entry.y) {
                        val tvList = listOf(
                            tvDayOfWeek,
                            tvDay,
                            tvSpending
                        )
                        tvList.forEach {
                            setAchievementTextColor(it)
                        }
                    }
                }
            }
        }

        private fun setAchievementTextColor(textView: TextView) {
            textView.setTextColor(
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.sub3
                    )
                )
            )
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        binding = ItemStatWeeklyWeekBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }
}

private class StatWeeklyWeekDiffUtil : DiffUtil.ItemCallback<StatWeeklyUiState.DayData>() {
    override fun areItemsTheSame(
        oldItem: StatWeeklyUiState.DayData,
        newItem: StatWeeklyUiState.DayData
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: StatWeeklyUiState.DayData,
        newItem: StatWeeklyUiState.DayData
    ): Boolean {
        return oldItem.dayOfWeek == oldItem.dayOfWeek
    }
}