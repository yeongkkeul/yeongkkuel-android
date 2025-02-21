package com.example.yeongkkuel.presentation.home.store

import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.yeongkkuel.databinding.ItemStoreProductBinding
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Product(
    val id: Int,
    val name: String,
    val price: Int,
    val imageUrl: String,
    val category: ProductCategory,
    val itemType: String ?
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
        Log.d("StoreAdapter", "어댑터에 전달된 Product - id: ${product.id}, name: ${product.name}, itemType: ${product.itemType}")


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
    fun clearSelection() {
        val previousPosition = selectedPosition
        selectedPosition = RecyclerView.NO_POSITION
        notifyItemChanged(previousPosition) // 선택 해제된 아이템만 업데이트
    }
    class StoreViewHolder(private val binding: ItemStoreProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product, isSelected: Boolean) {
            Glide.with(binding.root.context)
                .load(product.imageUrl) // 서버 이미지 URL 사용
                .into(binding.imgStoreProduct) //  이미지 로드

            binding.tvStoreProductName.text = product.name
            binding.tvProductPrice.text = product.price.toString()

            if (!isSelected) {
                binding.imgStoreCollect.visibility = View.GONE
            } else {
                binding.imgStoreCollect.visibility = View.VISIBLE
            }
        }
    }
}
