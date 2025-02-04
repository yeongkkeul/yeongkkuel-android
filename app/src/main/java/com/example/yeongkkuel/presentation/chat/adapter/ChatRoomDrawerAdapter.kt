package com.example.yeongkkuel.presentation.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.yeongkkuel.databinding.ItemChatRoomDrawerBinding
import com.example.yeongkkuel.presentation.chat.data.ChatRoomRank

class ChatRoomDrawerAdapter(
    private val chatRoomRank: ArrayList<ChatRoomRank>
) : RecyclerView.Adapter<ChatRoomDrawerAdapter.ChatRoomDrawerViewHolder>() {

    inner class ChatRoomDrawerViewHolder(val binding: ItemChatRoomDrawerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomDrawerViewHolder {
        val binding = ItemChatRoomDrawerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatRoomDrawerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatRoomDrawerViewHolder, position: Int) {
        val item = chatRoomRank[position]
        with(holder.binding) {
            Glide.with(ivChatRoomDrawerProfile.context)
                .load(item.profileImage)
                .apply(RequestOptions.circleCropTransform())
                .into(ivChatRoomDrawerProfile)
            tvChatRoomDrawerNickname.text = item.nickname
        }
    }

    override fun getItemCount(): Int = chatRoomRank.size
}