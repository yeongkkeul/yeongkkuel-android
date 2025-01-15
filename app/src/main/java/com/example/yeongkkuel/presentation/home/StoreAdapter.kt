package com.example.yeongkkuel.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.databinding.ItemStoreProductBinding

data class Product(val name: String, val price: Int, val imageResId: Int)

class StoreAdapter(private val products: List<Product>) :
    RecyclerView.Adapter<StoreAdapter.StoreViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreViewHolder {
        val binding = ItemStoreProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StoreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoreViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    inner class StoreViewHolder(private val binding: ItemStoreProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.imgStoreProduct.setImageResource(product.imageResId)
            binding.tvStoreProductName.text = product.name
            binding.tvProductPrice.text = "${product.price} 코인"
        }
    }
}