package com.example.yeongkkuel.presentation.stat.weekly.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.ItemStatWeeklyPiechartcategoryBinding
import com.example.yeongkkuel.presentation.botsheet.BotSheetUiState
import com.example.yeongkkuel.presentation.util.Colors
import com.example.yeongkkuel.presentation.util.toMoneyString

class StatWeeklyPieChartCategoryListAdapter(
) : ListAdapter<BotSheetUiState.Spending, StatWeeklyPieChartCategoryListAdapter.ViewHolder>(
    PieChartCategoryListDiffUtil()
) {
    inner class ViewHolder(
        private val binding: ItemStatWeeklyPiechartcategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: BotSheetUiState.Spending) = with(binding){
            val totalSpending = item.history.sumOf { it.price }

            tvSpendingCategory.text = item.kind.kor
            tvSpendingMoney.text = totalSpending.toMoneyString() + "원"

            when(item.color){
                Colors.BLUE -> ivStartPoint.setBackgroundResource(R.drawable.bg_point_blue)
                Colors.PINK -> ivStartPoint.setBackgroundResource(R.drawable.bg_point_pink)
                Colors.GREEN -> ivStartPoint.setBackgroundResource(R.drawable.bg_point_green)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            binding = ItemStatWeeklyPiechartcategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }
}

private class PieChartCategoryListDiffUtil :
    DiffUtil.ItemCallback<BotSheetUiState.Spending>() {

    override fun areItemsTheSame(
        oldItem: BotSheetUiState.Spending,
        newItem: BotSheetUiState.Spending
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: BotSheetUiState.Spending,
        newItem: BotSheetUiState.Spending
    ): Boolean {
        return oldItem.kind == newItem.kind && oldItem.color == newItem.color && oldItem.history == newItem.history
    }
}
