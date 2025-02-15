package com.example.yeongkkuel.presentation.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.ItemChatRoomRankBinding
import com.example.yeongkkuel.network.response.chat.ChatRoomRank
import com.example.yeongkkuel.presentation.chat.ChatRoomRankClickListener

class ChatRoomRankAdapter(
    private var chatRoomRank: List<ChatRoomRank>,
    private val listener: ChatRoomRankClickListener,
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
                tvChatRoomRankNo.text = item.rank.toString()
                tvChatRoomRankScore.text = "${item.rankScore}점"
            } else {
                tvChatRoomRankNo.text = "-"
                tvChatRoomRankScore.text = "-"
            }

            // 순위별 배경 및 텍스트 색상 지정 (API rank는 1부터 시작한다고 가정)
            val rankIndex = item.rank - 1
            when (rankIndex) {
                0 -> tvChatRoomRankNo.setBackgroundResource(R.drawable.bg_chat_room_rank_rankno_1)
                1 -> tvChatRoomRankNo.setBackgroundResource(R.drawable.bg_chat_room_rank_rankno_2)
                2 -> tvChatRoomRankNo.setBackgroundResource(R.drawable.bg_chat_room_rank_rankno_3)
                else -> tvChatRoomRankNo.setBackgroundResource(android.R.color.transparent)
            }

            tvChatRoomRankNo.setTextColor(
                when (rankIndex) {
                    0, 1, 2 -> ContextCompat.getColor(root.context, R.color.white)
                    else -> ContextCompat.getColor(root.context, R.color.black)
                }
            )

            tvChatRoomRankScore.setTextColor(
                when (rankIndex) {
                    0 -> ContextCompat.getColor(root.context, R.color.gold)
                    1 -> ContextCompat.getColor(root.context, R.color.silver)
                    2 -> ContextCompat.getColor(root.context, R.color.bronze)
                    else -> ContextCompat.getColor(root.context, R.color.main1)
                }
            )

            root.setOnClickListener {
                listener.onRankItemClick(item)
            }
        }
    }

    override fun getItemCount(): Int = chatRoomRank.size

    fun updateData(newData: List<ChatRoomRank>) {
        chatRoomRank = newData
        notifyDataSetChanged()
    }
}