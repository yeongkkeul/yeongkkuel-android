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
import android.util.Log


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
            ivBtnAdd.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, color))
            binding.ivBtnAdd.setImageResource(R.drawable.ic_plus_default)
            binding.ivBtnAdd.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, item.color.id))

            rvHistory.run {
                adapter = historyListAdapter
                historyListAdapter.submitList(item.history ?: emptyList())
                layoutManager = LinearLayoutManager(binding.root.context)
            }

            // + 버튼 클릭 리스너 추가
            ivBtnAdd.setOnClickListener {
                botSheetListener?.navigateToExpenseEntry(item.kind.kor, item.color.id)
                    ?: Log.e("BotSheetCategoryListAdapter", "botSheetListener is null")
            }

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder = ViewHolder(
        ItemBotsheetCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position) // 리스트 아이템을 가져옴
        holder.onBind(item)         // 아이템을 뷰홀더에 바인딩
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
        return oldItem.kind == newItem.kind && oldItem.color == newItem.color
    }
}
