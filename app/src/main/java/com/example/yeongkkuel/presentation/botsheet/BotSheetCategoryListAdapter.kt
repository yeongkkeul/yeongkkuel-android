package com.example.yeongkkuel.presentation.botsheet

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
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
import android.view.View
import androidx.navigation.Navigation.findNavController


class BotSheetCategoryListAdapter(
    private val botSheetListener: BotSheetListener
) : ListAdapter<BotSheetUiState.Spending, BotSheetCategoryListAdapter.ViewHolder>(
    SpendingCategoryListDiffUtil()
) {

    inner class ViewHolder(
        private val binding: ItemBotsheetCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: BotSheetUiState.Spending) = with(binding) {
            // 카테고리 색상 등 item 정보를 여기서 가져올 수 있음
            val categoryColor = item.color.id

            // ✅ onBind() 내부에서 historyListAdapter 생성
            val localHistoryListAdapter = BotSheetHistoryListAdapter { selectedHistory ->
                // item.color.id를 여기서 쓸 수 있음!
                botSheetListener.navigateToExpenseView(
                    expenseId = selectedHistory.id,
                    expenseName = selectedHistory.name,
                    expensePrice = selectedHistory.price,
                    categoryColor = categoryColor,
                    categoryName = item.kind.name
                )
            }

            rvHistory.run {
                adapter = localHistoryListAdapter
                localHistoryListAdapter.submitList(item.history ?: emptyList()) {
                    // ✅ 최신 데이터 반영 후 UI 업데이트
                    updateNoSpendAndMoreVisibility(item)
                }
                layoutManager = LinearLayoutManager(root.context)
            }

            tvCategory.text = item.kind.name
            val context = root.context
            tvCategory.setTextColor(ContextCompat.getColor(context, categoryColor))
            ivBtnAdd.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, categoryColor))
            ivBtnAdd.setImageResource(R.drawable.ic_plus_default)
            ivBtnAdd.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, categoryColor))

            // 🔹 카테고리 추가 버튼 클릭 리스너
            ivBtnAdd.setOnClickListener {
                botSheetListener.navigateToExpenseEntry(item.kind.name, categoryColor)
            }
        }

        private fun updateNoSpendAndMoreVisibility(item: BotSheetUiState.Spending) {
            val isEmpty = item.history.isEmpty()

            if (isEmpty) {
                binding.tvNoSpend.visibility = View.GONE
                botSheetListener.onNoExpenseChanged(false)
            } else {
                binding.tvNoSpend.visibility = View.VISIBLE
                botSheetListener.onNoExpenseChanged(true)
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
        return oldItem.categoryId == newItem.categoryId
    }

    override fun areContentsTheSame(
        oldItem: BotSheetUiState.Spending,
        newItem: BotSheetUiState.Spending
    ): Boolean {
        return oldItem.kind == newItem.kind && oldItem.color == newItem.color
    }
}