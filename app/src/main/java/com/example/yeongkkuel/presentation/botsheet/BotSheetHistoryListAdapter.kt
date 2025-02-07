package com.example.yeongkkuel.presentation.botsheet

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.ItemBotsheetHistoryBinding
import com.example.yeongkkuel.presentation.util.toMoneyString

class BotSheetHistoryListAdapter(
    private val onItemClick: (BotSheetUiState.Spending.History) -> Unit
) : ListAdapter<BotSheetUiState.Spending.History, BotSheetHistoryListAdapter.ViewHolder>(
    SpendingHistoryListDiffUtil()
) {
    inner class ViewHolder(
        private val binding: ItemBotsheetHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: BotSheetUiState.Spending.History) = with(binding) {
            // 이름과 가격 설정
            tvName.text = item.name
            tvPrice.text = "-${item.price.toMoneyString()}원"

            tvName.text = if (item.name.length > 9) {
                "${item.name.take(9)}..."
            } else {
                item.name
            }

            if (!item.imgExist) {
                icPhotoIncluded.visibility = View.VISIBLE // 사진이 있을 경우 표시
            } else {
                icPhotoIncluded.visibility = View.GONE // 사진이 없을 경우 숨김
            }
            root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
    private class SpendingHistoryListDiffUtil : DiffUtil.ItemCallback<BotSheetUiState.Spending.History>() {
        override fun areItemsTheSame(
            oldItem: BotSheetUiState.Spending.History,
            newItem: BotSheetUiState.Spending.History
        ): Boolean {
            return oldItem.name == newItem.name && oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: BotSheetUiState.Spending.History,
            newItem: BotSheetUiState.Spending.History
        ): Boolean {
            return oldItem == newItem
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