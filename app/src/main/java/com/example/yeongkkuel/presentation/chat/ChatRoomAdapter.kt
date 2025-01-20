package com.example.yeongkkuel.presentation.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.ItemChatRoomBinding

class ChatRoomAdapter(
    val chatRooms: ArrayList<ChatRoom>,
    private val listener: ChatRoomClickListener
) : RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        val binding: ItemChatRoomBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_chat_room,
            parent,
            false
        )
        return ChatRoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        val chatRoom = chatRooms[position]
        holder.bind(chatRoom)
    }

    override fun getItemCount(): Int = chatRooms.size

    inner class ChatRoomViewHolder(private val binding: ItemChatRoomBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(chatRoom: ChatRoom) {
            binding.apply {
                Glide.with(ivThumbnail.context)
                    .load(chatRoom.thumbnailUrl) // URL에서 이미지 로드
                    .apply(RequestOptions.bitmapTransform(RoundedCorners(12))) // 둥근 테두리 적용
                    .into(ivThumbnail)
                tvTitleChatRoom.text = chatRoom.title
                tvThumbnailMessage.text = chatRoom.recentMessage
                tvTimeMessage.text = chatRoom.messageTime
                tvAmountPeople.text = chatRoom.participantCount.toString()
                root.setOnClickListener {
                    listener.onItemClicked(chatRoom)
                }
            }

            binding.layoutDelete.setOnClickListener {
                removeItem(this.layoutPosition)
                listener.onItemDeleted(chatRoom)
                Toast.makeText(binding.root.context, "삭제되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 데이터 갱신 메서드
    fun updateData(newData: List<ChatRoom>) {
        chatRooms.clear()
        chatRooms.addAll(newData)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        if (position >= 0 && position < chatRooms.size) {
            chatRooms.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, chatRooms.size) // 아이템을 제거한 이후의 아이템들에 대해 포지션 업데이트
        }
    }
}