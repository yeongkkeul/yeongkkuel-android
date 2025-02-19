package com.example.yeongkkuel.presentation.botsheet

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
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
import com.example.yeongkkuel.presentation.base.MainActivity
import com.example.yeongkkuel.presentation.util.dpToPx


class BotSheetCategoryListAdapter(
    private val botSheetListener: BotSheetListener
) : ListAdapter<BotSheetUiState.Spending, BotSheetCategoryListAdapter.ViewHolder>(
    SpendingCategoryListDiffUtil()
) {

    inner class ViewHolder(
        val binding: ItemBotsheetCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: BotSheetUiState.Spending) = with(binding) {
            val currentTab = (root.context as? MainActivity)?.getCurrentTab() ?: "home" // 기본값 home

            ivBtnAdd.setOnClickListener {
                // 지출 기입 페이지로 이동하면서 데이터 전달
                botSheetListener.navigateToExpenseEntryWithTab(currentTab, item.kind.name, item.color.id)
            }

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
        }


        private fun updateNoSpendAndMoreVisibility(histories: List<BotSheetUiState.Spending.History>) {
            // 인자로 '원본 목록'을 받음 (blank+0원 항목도 포함)
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
        val item = getItem(position)
        val layoutParams = holder.itemView.layoutParams as RecyclerView.LayoutParams

        if (item.kind.name == "trash") {
            if (item.history.isEmpty()) {
                // Trash 카테고리에 지출 내역이 없으면 완전히 숨김
                holder.itemView.visibility = View.GONE
                layoutParams.height = 0
                layoutParams.topMargin = 0
                layoutParams.bottomMargin = 0
            } else {
                // Trash 카테고리에 지출 내역이 있으면 가장 하단에 위치 (카테고리명과 +버튼 숨김)
                holder.itemView.visibility = View.VISIBLE
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT

                // 기존 간격 유지하면서 하단 여백 추가
                layoutParams.topMargin = 8.dpToPx(holder.itemView.context)
                layoutParams.bottomMargin = 8.dpToPx(holder.itemView.context)

                // Trash 카테고리 내부의 UI 요소 숨기기 (공간 완전 제거)
                holder.binding.tvCategory.visibility = View.GONE
                holder.binding.ivBtnAdd.visibility = View.GONE
                holder.binding.ivPlus.visibility = View.GONE

                // rvHistory에 하단 마진 추가
                val rvParams = holder.binding.rvHistory.layoutParams as ViewGroup.MarginLayoutParams
                rvParams.bottomMargin = 5.dpToPx(holder.itemView.context)  // ✅ 내부 간격 조정!!
                holder.binding.rvHistory.layoutParams = rvParams
            }
        } else {
            // 일반 카테고리는 기존과 동일하게 표시됨
            holder.itemView.visibility = View.VISIBLE
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT

            // 기존 간격 유지
            layoutParams.topMargin = 8.dpToPx(holder.itemView.context)
            layoutParams.bottomMargin = 8.dpToPx(holder.itemView.context)

            holder.binding.tvCategory.visibility = View.VISIBLE
            holder.binding.ivBtnAdd.visibility = View.VISIBLE
            holder.binding.ivPlus.visibility = View.VISIBLE

            // 일반 카테고리는 `rvHistory` 하단 마진 원래대로 (기본값 유지)
            val rvParams = holder.binding.rvHistory.layoutParams as ViewGroup.MarginLayoutParams
            rvParams.bottomMargin = 0 // 기존 값 유지
            holder.binding.rvHistory.layoutParams = rvParams
        }

        holder.itemView.layoutParams = layoutParams
        holder.onBind(item)
    }


    fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }


    override fun submitList(list: List<BotSheetUiState.Spending>?) {
        // 실제 노출될 카테고리만 전달 (trash 카테고리는 내역이 없으면 제외)
        val filteredList = list?.filter { spending ->
            // trash 카테고리는 내역이 있거나, 아니면 제거
            if (spending.kind.name.lowercase() == "trash") {
                spending.history.isNotEmpty()
            } else {
                true
            }
        }
        val sortedSpendingList = filteredList?.sortedBy { spending ->
            if (spending.kind.name.lowercase() == "trash") 1 else 0
        }
        super.submitList(sortedSpendingList)
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