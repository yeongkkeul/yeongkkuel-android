package com.example.yeongkkuel.presentation.stat.weekly

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.databinding.ItemStatWeeklyWeekBinding
import com.example.yeongkkuel.presentation.toMoneyString
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale

class StatWeeklyWeekListAdapter(
) : ListAdapter<StatWeeklyUiState.DayData, StatWeeklyWeekListAdapter.ViewHolder>(
    StatWeeklyWeekDiffUtil()
) {
    inner class ViewHolder(
        private val binding: ItemStatWeeklyWeekBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: StatWeeklyUiState.DayData) = with(binding) {
            tvDayOfWeek.text = item.dayOfWeek.kor

            // 요일에 맞춘 날짜를 계산하여 표시
            val today = Calendar.getInstance()
            val dayOfWeekIndex = item.dayOfWeek.ordinal + 1  // Calendar 요일은 1~7 (일~토)
            today.set(Calendar.DAY_OF_WEEK, dayOfWeekIndex)

            val dayOfMonth = today.get(Calendar.DAY_OF_MONTH)
            tvDay.text = dayOfMonth.toMoneyString()


            // 지출 금액을 백의자리 올림으로 n.n만 형식으로 표시
            if (item.totalSpending != null) {
                val spendingInTenThousand = (item.totalSpending / 10000.0).toBigDecimal()
                    .setScale(1, RoundingMode.UP)  // 백의자리 올림
                tvSpending.text = "${spendingInTenThousand}만"
            } else {
                tvSpending.text = "-"
            }

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