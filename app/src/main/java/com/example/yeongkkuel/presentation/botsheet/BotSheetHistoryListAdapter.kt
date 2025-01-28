package com.example.yeongkkuel.presentation.botsheet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.databinding.ItemBotsheetHistoryBinding
import com.example.yeongkkuel.presentation.util.toMoneyString

class BotSheetHistoryListAdapter(
) : ListAdapter<BotSheetUiState.Spending.History, BotSheetHistoryListAdapter.ViewHolder>(
    SpendingHistoryListDiffUtil()
) {
    inner class ViewHolder(
        private val binding: ItemBotsheetHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: BotSheetUiState.Spending.History) = with(binding) {
            tvName.text = item.name
            tvPrice.text = "-" + item.price.toMoneyString() + "원"
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder = ViewHolder(
        binding = ItemBotsheetHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }
}

private class SpendingHistoryListDiffUtil : DiffUtil.ItemCallback<BotSheetUiState.Spending.History>() {

    override fun areItemsTheSame(
        oldItem: BotSheetUiState.Spending.History,
        newItem: BotSheetUiState.Spending.History
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: BotSheetUiState.Spending.History,
        newItem: BotSheetUiState.Spending.History
    ): Boolean {
        return oldItem.name == newItem.name && oldItem.price == newItem.price
    }
}