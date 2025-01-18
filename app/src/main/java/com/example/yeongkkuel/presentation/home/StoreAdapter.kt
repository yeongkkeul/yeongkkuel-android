package com.example.yeongkkuel.presentation.home

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.databinding.ItemStoreProductBinding

data class Product(
    val name: String,
    val price: Int,
    val imageResId: Int,
    val category: ProductCategory
)

class StoreAdapter(
    private val products: List<Product>,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<StoreAdapter.StoreViewHolder>() {

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
        holder.bind(product)
        holder.itemView.setOnClickListener {
            Log.d("StoreAdapter", "Selected Product: ${product.name}, ResId: ${product.imageResId}")
            onItemClick(product)
        }
    }

    override fun getItemCount(): Int = products.size

    inner class StoreViewHolder(private val binding: ItemStoreProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.imgStoreProduct.setImageResource(product.imageResId)
            binding.tvStoreProductName.text = product.name
        }
    }
}
