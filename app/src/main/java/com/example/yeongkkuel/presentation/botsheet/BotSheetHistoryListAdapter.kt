package com.example.yeongkkuel.presentation.botsheet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
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

            if (item.price > 0) {
                // 금액이 1원 이상이면 기존처럼 표기
                tvPrice.text = "-${item.price.toMoneyString()}원"
            } else {
                // 금액이 0원이면 가격 부분 숨기거나 "" 처리
                tvPrice.text = ""
            }

            // 만약 "내용이 빈칸 + 금액 0원"도 표시하고 싶지 않다면, 여기서 추가 처리
             if (item.name.isBlank() && item.price == 0) {
                 root.visibility = View.GONE
             } else {
                 root.visibility = View.VISIBLE
             }

            if (item.imgExist) {
                icPhotoIncluded.visibility = View.VISIBLE
            } else {
                icPhotoIncluded.visibility = View.GONE
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