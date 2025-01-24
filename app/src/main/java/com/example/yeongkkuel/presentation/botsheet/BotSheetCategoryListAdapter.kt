package com.example.yeongkkuel.presentation.botsheet

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.ItemBotsheetCategoryBinding


class BotSheetCategoryListAdapter(
    private val botSheetListener: BotSheetListener
) : ListAdapter<BotSheetUiState.Spending, BotSheetCategoryListAdapter.ViewHolder>(
    SpendingCategoryListDiffUtil()
) {
    inner class ViewHolder(
        private val binding: ItemBotsheetCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val historyListAdapter = BotSheetHistoryListAdapter()
        fun onBind(item: BotSheetUiState.Spending) = with(binding) {
            tvCategory.text = item.kind.kor
            val context = binding.root.context
            val color = item.color.id // Colors Enum의 id 사용
            tvCategory.setTextColor(ContextCompat.getColor(context, color))
            ivBtnAdd.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, color))
            binding.ivBtnAdd.setImageResource(R.drawable.ic_plus_default)
            binding.ivBtnAdd.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, item.color.id))


            rvHistory.run {
                adapter = historyListAdapter
                historyListAdapter.submitList(item.history)
                layoutManager = LinearLayoutManager(binding.root.context)
            }
            // + 버튼 클릭 리스너 추가
            ivBtnAdd.setOnClickListener {
                botSheetListener.navigateToExpenseEntry() // BotSheetListener의 메서드를 호출해 MainActivity로 이벤트 전달
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

class SpendingCategoryListDiffUtil : DiffUtil.ItemCallback<BotSheetUiState.Spending>() {

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
        return  oldItem.kind == newItem.kind && oldItem.color == newItem.color
    }
}