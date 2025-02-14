package com.example.yeongkkuel.presentation.home.category.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.databinding.ItemCategoryBinding
import com.example.yeongkkuel.presentation.home.category.data.Category

class CategoryAdapter(
    private val onCategoryClick: (Category) -> Unit,
    private val onStartDrag: (RecyclerView.ViewHolder) -> Unit // 드래그 시작 콜백 추가
) : ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) { // ListAdapter로 변경

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Category) = with(binding) {
            tvCategoryName.text = item.name

            // Colors Enum에서 색상 ID를 가져와 색상 설정
            val colorInt = ContextCompat.getColor(root.context, item.color.id)
            tvCategoryName.setTextColor(colorInt)

            // 클릭 이벤트 처리
            root.setOnClickListener {
                onCategoryClick(item) // 클릭 이벤트 실행
            }

            // 길게 눌렀을 때 드래그 시작
            root.setOnLongClickListener {
                onStartDrag(this@CategoryViewHolder) // 드래그 시작
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = getItem(position) // `getItem(position)`으로 아이템 가져오기
        holder.bind(category)
    }

    override fun getItemCount(): Int = currentList.size // ListAdapter에서는 currentList 사용

    // 드래그하여 순서 변경하는 함수
    fun moveItem(fromPosition: Int, toPosition: Int) {
        val currentList = currentList.toMutableList() // 현재 리스트를 MutableList로 변환
        val movedItem = currentList.removeAt(fromPosition)
        currentList.add(toPosition, movedItem)

        // 데이터 변경 후 submitList()로 RecyclerView 갱신
        submitList(currentList)
    }

    // 현재 리스트 반환
    fun getCategoryList(): List<Category> = currentList.toList()

    // DiffUtil을 이용하여 변경 사항만 갱신하도록 처리
    class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id // ID로 고유성 판단
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem // 데이터 클래스 전체 비교
        }
    }
}