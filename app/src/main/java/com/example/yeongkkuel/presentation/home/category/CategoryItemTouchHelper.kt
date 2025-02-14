package com.example.yeongkkuel.presentation.home.category

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.presentation.home.category.adapter.CategoryAdapter

// TODO - 다른 기능 구현 끝나고 나서 파일 분리 시작 하자

class CategoryItemTouchHelper(
    private val categoryAdapter: CategoryAdapter
) : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val fromPosition = viewHolder.bindingAdapterPosition
        val toPosition = target.bindingAdapterPosition
        categoryAdapter.moveItem(fromPosition, toPosition) // UI 순서 변경
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // 스와이프 기능 비활성화
    }

    // 드래그 후 아이템을 업데이트할 때 호출
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)

        val updatedList = categoryAdapter.getCategoryList()
        // ViewModel에 변경된 카테고리 순서 반영
        categoryAdapter.submitList(updatedList)
    }

    // 롱클릭으로 드래그 활성화
    override fun isLongPressDragEnabled(): Boolean = true

    // 스와이프 비활성화
    override fun isItemViewSwipeEnabled(): Boolean = false
}
