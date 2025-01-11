package com.example.yeongkkuel.presentation.statbotsheet

import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class StatBotSheetItemTouchHelper(
  private val adapter: StatBotSheetCategoryListAdapter
) : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT
) {
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val fromPosition = viewHolder.adapterPosition
        val toPosition = target.adapterPosition

        adapter.moveItem(fromPosition, toPosition)

        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

    }

    // 롱클릭으로 드래그를 활성화
    override fun isLongPressDragEnabled(): Boolean = true

    // 스와이프 비활성화
    override fun isItemViewSwipeEnabled(): Boolean = false

}