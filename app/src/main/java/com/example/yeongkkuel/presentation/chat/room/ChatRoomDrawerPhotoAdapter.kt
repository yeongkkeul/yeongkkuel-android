package com.example.yeongkkuel.presentation.chat.room

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.yeongkkuel.databinding.ItemChatRoomDrawerPhotoBinding

class ChatRoomDrawerPhotoAdapter(private val imageUrls: List<String>) :
    RecyclerView.Adapter<ChatRoomDrawerPhotoAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(private val binding: ItemChatRoomDrawerPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(imageUrl: String) {
            Glide.with(binding.root.context)
                .load(imageUrl)
                .into(binding.ivImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemChatRoomDrawerPhotoBinding.inflate(inflater, parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(imageUrls[position])
    }

    override fun getItemCount(): Int = imageUrls.size
}