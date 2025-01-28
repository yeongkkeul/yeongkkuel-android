package com.example.yeongkkuel.presentation.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.ItemChatRoomRankBinding
import com.example.yeongkkuel.presentation.chat.data.ChatRoomRank

class ChatRoomRankAdapter(
    private val chatRoomRank: ArrayList<ChatRoomRank>
) : RecyclerView.Adapter<ChatRoomRankAdapter.ChatRoomRankViewHolder>() {

    inner class ChatRoomRankViewHolder(val binding: ItemChatRoomRankBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomRankViewHolder {
        val binding = ItemChatRoomRankBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatRoomRankViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatRoomRankViewHolder, position: Int) {
        val item = chatRoomRank[position]
        with(holder.binding) {
            Glide.with(ivChatRoomRankProfile.context)
                .load(item.profileImage)
                .apply(RequestOptions.circleCropTransform())
                .into(ivChatRoomRankProfile)
            tvChatRoomRankNickname.text = item.nickname

            if (position < 10) {
                tvChatRoomRankNo.text = (position + 1).toString()
                tvChatRoomRankScore.text = "${item.rankScore}점"
            } else {
                tvChatRoomRankNo.text = "-"
                tvChatRoomRankScore.text = "-"
            }

            when (position) {
                0 -> tvChatRoomRankNo.setBackgroundResource(R.drawable.bg_chat_room_rank_rankno_1)
                1 -> tvChatRoomRankNo.setBackgroundResource(R.drawable.bg_chat_room_rank_rankno_2)
                2 -> tvChatRoomRankNo.setBackgroundResource(R.drawable.bg_chat_room_rank_rankno_3)
                else -> tvChatRoomRankNo.setBackgroundResource(android.R.color.transparent)
            }

            tvChatRoomRankNo.setTextColor(
                when (position) {
                    0 -> ContextCompat.getColor(root.context, R.color.white)
                    1 -> ContextCompat.getColor(root.context, R.color.white)
                    2 -> ContextCompat.getColor(root.context, R.color.white)
                    else -> ContextCompat.getColor(root.context, R.color.black)
                }
            )

            tvChatRoomRankScore.setTextColor(
                when (position) {
                    0 -> ContextCompat.getColor(root.context, R.color.gold)
                    1 -> ContextCompat.getColor(root.context, R.color.silver)
                    2 -> ContextCompat.getColor(root.context, R.color.bronze)
                    else -> ContextCompat.getColor(root.context, R.color.main1)
                }
            )
        }
    }

    override fun getItemCount(): Int = chatRoomRank.size
}