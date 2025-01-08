package com.example.yeongkkuel.presentation.statbotsheet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.databinding.ItemBotsheetHistoryBinding
import com.example.yeongkkuel.presentation.toMoneyString

class StatBotSheetHistoryListAdapter(
) : ListAdapter<StatBotSheetUiState.Spending.History, StatBotSheetHistoryListAdapter.ViewHolder>(
    SpendingHistoryListDiffUtil()
) {
    inner class ViewHolder(
        private val binding: ItemBotsheetHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: StatBotSheetUiState.Spending.History) = with(binding) {
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

class SpendingHistoryListDiffUtil : DiffUtil.ItemCallback<StatBotSheetUiState.Spending.History>() {

    override fun areItemsTheSame(
        oldItem: StatBotSheetUiState.Spending.History,
        newItem: StatBotSheetUiState.Spending.History
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: StatBotSheetUiState.Spending.History,
        newItem: StatBotSheetUiState.Spending.History
    ): Boolean {
        return oldItem.name == newItem.name
    }
}