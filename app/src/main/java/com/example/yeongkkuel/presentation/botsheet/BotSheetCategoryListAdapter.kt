package com.example.yeongkkuel.presentation.botsheet

import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.ItemBotsheetCategoryBinding
import android.view.View


class BotSheetCategoryListAdapter(
    private val botSheetListener: BotSheetListener
) : ListAdapter<BotSheetUiState.Spending, BotSheetCategoryListAdapter.ViewHolder>(
    SpendingCategoryListDiffUtil()
) {

    inner class ViewHolder(
        private val binding: ItemBotsheetCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: BotSheetUiState.Spending) = with(binding) {
            // 카테고리 색상 가져오기
            val categoryColor = item.color.id

            // 1) 원본 목록 (blank+0원 항목도 들어있음)
            val originalHistories = item.history

            // 2) UI에 표시할 목록 (blank+0원 항목 필터링)
            val displayedHistories = originalHistories.filterNot { hist ->
                hist.name.isBlank() && hist.price == 0
            }

            Log.e("histories2", originalHistories.toString())

            // onBind() 안에서 새로 Adapter 생성
            val localHistoryListAdapter = BotSheetHistoryListAdapter { selectedHistory ->
                botSheetListener.navigateToExpenseView(
                    expenseId = selectedHistory.id,
                    expenseName = selectedHistory.name,
                    expensePrice = selectedHistory.price,
                    categoryColor = categoryColor,
                    categoryName = item.kind.name,
                    imageUrl = selectedHistory.imgExist
                )
            }

            rvHistory.run {
                adapter = localHistoryListAdapter
                // 필터링된 목록만 표시!
                localHistoryListAdapter.submitList(displayedHistories) {
                    // 무지출 문구는 '원본 목록'으로 판단
                    updateNoSpendAndMoreVisibility(originalHistories)
                }
                layoutManager = LinearLayoutManager(root.context)
            }


            // UI 세팅
            tvCategory.text = item.kind.name
            val context = root.context
            tvCategory.setTextColor(ContextCompat.getColor(context, categoryColor))
            ivBtnAdd.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, categoryColor))
            ivBtnAdd.setImageResource(R.drawable.ic_plus_default)
            ivBtnAdd.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, categoryColor))

            // 카테고리 추가 버튼 클릭 리스너
            ivBtnAdd.setOnClickListener {
                botSheetListener.navigateToExpenseEntry(item.kind.name, categoryColor)
            }
        }


        private fun updateNoSpendAndMoreVisibility(histories: List<BotSheetUiState.Spending.History>) {
            // ⚠ 인자로 '원본 목록'을 받음 (blank+0원 항목도 포함)
            if (histories.isEmpty()) {
                // 내역이 전혀 없으면 → 무지출 문구 안 보임
                binding.tvNoSpend.visibility = View.GONE
                botSheetListener.onNoExpenseChanged(false)
                return
            }

            // 내역이 있고, 모두 `price == 0`이면 무지출 문구 보이기
            val allZero = histories.all { it.price == 0 }

            if (allZero) {
                binding.tvNoSpend.visibility = View.VISIBLE
                botSheetListener.onNoExpenseChanged(true)
            } else {
                binding.tvNoSpend.visibility = View.GONE
                botSheetListener.onNoExpenseChanged(false)
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
        return oldItem.kind == newItem.kind &&
                oldItem.color == newItem.color &&
                oldItem.plusIconResId == newItem.plusIconResId &&
                oldItem.history.size == newItem.history.size &&
                oldItem.history.zip(newItem.history).all { (old, new) ->
                    old == new
                }
    }

}