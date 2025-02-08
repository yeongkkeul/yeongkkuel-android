package com.example.yeongkkuel.presentation.stat.weekly.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.ItemStatWeeklyPiechartcategoryBinding
import com.example.yeongkkuel.presentation.stat.weekly.StatWeeklyUiState
import com.example.yeongkkuel.presentation.util.Colors
import com.example.yeongkkuel.presentation.util.toMoneyString

class StatWeeklyPieChartCategoryListAdapter(
) : ListAdapter<StatWeeklyUiState.PieChartData, StatWeeklyPieChartCategoryListAdapter.ViewHolder>(
    PieChartCategoryListDiffUtil()
) {
    inner class ViewHolder(
        private val binding: ItemStatWeeklyPiechartcategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: StatWeeklyUiState.PieChartData) = with(binding){
            if(item.category.kor == "") binding.root.visibility = View.GONE
            else {
                tvSpendingCategory.text = item.category.kor
                tvSpendingMoney.text = item.expenditure.toMoneyString() + "원"

                tvSpendingMoney.setTextColor(item.color.rgb)
                ivStartPoint.setColorFilter(item.color.rgb)
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
    DiffUtil.ItemCallback<StatWeeklyUiState.PieChartData>() {

    override fun areItemsTheSame(
        oldItem: StatWeeklyUiState.PieChartData,
        newItem: StatWeeklyUiState.PieChartData
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: StatWeeklyUiState.PieChartData,
        newItem: StatWeeklyUiState.PieChartData
    ): Boolean {
        return oldItem.category == newItem.category && oldItem.color == newItem.color
    }
}
