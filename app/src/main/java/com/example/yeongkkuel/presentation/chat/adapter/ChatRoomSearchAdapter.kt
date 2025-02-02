package com.example.yeongkkuel.presentation.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.ItemChatRoomSearchBinding
import com.example.yeongkkuel.presentation.chat.ChatRoomSearch
import com.example.yeongkkuel.presentation.chat.ChatRoomSearchClickListener

class ChatRoomSearchAdapter(
    private var chatRoomsSearch: List<ChatRoomSearch>,
    private val listener: ChatRoomSearchClickListener,
) : RecyclerView.Adapter<ChatRoomSearchAdapter.ChatRoomSearchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomSearchViewHolder {
        val binding: ItemChatRoomSearchBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_chat_room_search,
            parent,
            false
        )
        return ChatRoomSearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatRoomSearchViewHolder, position: Int) {
        val chatRoom = chatRoomsSearch[position]
        holder.bind(chatRoom)
    }

    override fun getItemCount(): Int = chatRoomsSearch.size

    inner class ChatRoomSearchViewHolder(private val binding: ItemChatRoomSearchBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(chatRoomSearch: ChatRoomSearch) {
            binding.apply {
                Glide.with(ivThumbnail.context)
                    .load(chatRoomSearch.chatRoomThumbnail)
                    .into(ivThumbnail)
                tvTitleChatRoom.text = chatRoomSearch.chatRoomName
                tvChatRoomTagAge.text = chatRoomSearch.chatRoomAgeRange
                tvChatRoomTagStatus.text = chatRoomSearch.chatRoomJob
                tvChatRoomTagDays.text = "${chatRoomSearch.chatRoomDDay}일째"
                tvChatRoomGoalExpense.text = chatRoomSearch.chatRoomSpendingAmount.toString()
                tvChatRoomAmountPeople.text = chatRoomSearch.chatRoomMaxUserCount
                root.setOnClickListener {
                    listener.onItemClicked(chatRoomSearch)
                }
            }
        }
    }

    fun updateList(newList: List<ChatRoomSearch>) {
        chatRoomsSearch = newList
        notifyDataSetChanged()
    }
}