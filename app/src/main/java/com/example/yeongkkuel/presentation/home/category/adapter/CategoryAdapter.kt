package com.example.yeongkkuel.presentation.home.category.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.databinding.ItemCategoryBinding
import com.example.yeongkkuel.presentation.home.category.data.Category

class CategoryAdapter(
    private val onCategoryClick: (Category) -> Unit // 클릭 이벤트 전달
) : ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

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
        holder.bind(getItem(position)) // 데이터 바인딩
    }

    class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id // ID로 고유성 판단
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem // 데이터 클래스 전체 비교
        }
    }
}
