package com.example.yeongkkuel.presentation.home.store

import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.databinding.ItemStoreProductBinding
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Product(
    val id: Int, // ✅ id 추가
    val name: String,
    val price: Int,
    val imageResId: Int,
    val category: ProductCategory,
    var area: String? = null
) : Parcelable

class StoreAdapter(
    private val products: List<Product>,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<StoreAdapter.StoreViewHolder>() {
    private var selectedPosition: Int = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreViewHolder {
        val binding = ItemStoreProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StoreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoreViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product, position == selectedPosition)

        // 아이템 클릭 이벤트 처리
        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position

            // 이전 선택된 아이템과 현재 선택된 아이템만 업데이트
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)

            // 클릭된 상품 전달
            onItemClick(product)
        }
    }

    override fun getItemCount(): Int = products.size

    inner class StoreViewHolder(private val binding: ItemStoreProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product, isSelected: Boolean) {
            binding.imgStoreProduct.setImageResource(product.imageResId)
            binding.tvStoreProductName.text = product.name

            binding.imgStoreCollect.visibility = if (isSelected) View.VISIBLE else View.GONE

        }
    }
}
