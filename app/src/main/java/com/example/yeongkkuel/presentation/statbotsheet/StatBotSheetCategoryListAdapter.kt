package com.example.yeongkkuel.presentation.statbotsheet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.databinding.ItemBotsheetCategoryBinding

class StatBotSheetCategoryListAdapter(
) : ListAdapter<StatBotSheetUiState.Spending, StatBotSheetCategoryListAdapter.ViewHolder>(
    SpendingCategoryListDiffUtil()
) {
    inner class ViewHolder(
        private val binding: ItemBotsheetCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val historyListAdapter = StatBotSheetHistoryListAdapter()

        fun onBind(item: StatBotSheetUiState.Spending) = with(binding) {
            tvCategory.text = item.kind
            tvCategory.setTextColor(ContextCompat.getColor(binding.root.context, item.color))

            rvHistory.run {
                adapter = historyListAdapter
                historyListAdapter.submitList(item.history)
                layoutManager = LinearLayoutManager(binding.root.context)
            }
        }
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        val currentList = currentList.toMutableList()
        val item = currentList.removeAt(fromPosition)
        currentList.add(toPosition, item)

        submitList(currentList)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder = ViewHolder(
        binding = ItemBotsheetCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }
}

class SpendingCategoryListDiffUtil : DiffUtil.ItemCallback<StatBotSheetUiState.Spending>() {

    override fun areItemsTheSame(
        oldItem: StatBotSheetUiState.Spending,
        newItem: StatBotSheetUiState.Spending
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: StatBotSheetUiState.Spending,
        newItem: StatBotSheetUiState.Spending
    ): Boolean {
        return  oldItem.kind == newItem.kind && oldItem.color == newItem.color
    }
}