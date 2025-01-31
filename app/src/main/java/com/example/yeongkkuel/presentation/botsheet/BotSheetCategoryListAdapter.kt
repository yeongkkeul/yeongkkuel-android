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
import androidx.navigation.Navigation.findNavController


class BotSheetCategoryListAdapter(
    private val botSheetListener: BotSheetListener
) : ListAdapter<BotSheetUiState.Spending, BotSheetCategoryListAdapter.ViewHolder>(
    SpendingCategoryListDiffUtil()
) {

    inner class ViewHolder(
        private val binding: ItemBotsheetCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        // 🔹 클릭 리스너를 Adapter에 직접 추가하지 않고, Fragment로 전달
        private val historyListAdapter = BotSheetHistoryListAdapter { selectedHistory ->
            val colorInt = try {
                Color.parseColor(selectedHistory.categoryColor) // ✅ String(HEX) → Int 변환
            } catch (e: IllegalArgumentException) {
                Log.e("BotSheetHistoryListAdapter", "Invalid color format: ${selectedHistory.categoryColor}", e)
                Color.RED // 기본값 검정색 적용
            }

            botSheetListener.navigateToExpenseView(
                selectedHistory.name,
                selectedHistory.price,
                colorInt // ✅ Int 값으로 변환 후 전달
            )
        }

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

            // 🔹 카테고리 추가 버튼 클릭 리스너
            ivBtnAdd.setOnClickListener {
                botSheetListener.navigateToExpenseEntry(item.kind.kor, item.color.id)
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
