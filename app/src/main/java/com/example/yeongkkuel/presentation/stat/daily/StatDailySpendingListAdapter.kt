package com.example.yeongkkuel.presentation.stat.daily

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.databinding.ItemStatDailySpendingBinding
import com.example.yeongkkuel.presentation.toMoneyString

class StatDailySpendingListAdapter(
) : ListAdapter<StatDailyUiState.SpendingList.Spending, StatDailySpendingListAdapter.ViewHolder>(
    SpendingListDiffUtil()
) {
    inner class ViewHolder(
        private val binding: ItemStatDailySpendingBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: StatDailyUiState.SpendingList.Spending) = with(binding) {
            tvName.text = item.kind
            tvPrice.text = "- " + item.price.toMoneyString() + "원"
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StatDailySpendingListAdapter.ViewHolder = ViewHolder(
        binding = ItemStatDailySpendingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: StatDailySpendingListAdapter.ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }
}

class SpendingListDiffUtil : DiffUtil.ItemCallback<StatDailyUiState.SpendingList.Spending>() {

    override fun areItemsTheSame(
        oldItem: StatDailyUiState.SpendingList.Spending,
        newItem: StatDailyUiState.SpendingList.Spending
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: StatDailyUiState.SpendingList.Spending,
        newItem: StatDailyUiState.SpendingList.Spending
    ): Boolean {
        return oldItem.kind == newItem.kind
    }
}