package com.example.yeongkkuel.presentation.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.ItemChatRoomSearchBinding
import com.example.yeongkkuel.network.response.chat.ChatRoomDetailDto
import com.example.yeongkkuel.presentation.chat.data.Age
import com.example.yeongkkuel.presentation.chat.data.Job
import com.example.yeongkkuel.presentation.chat.search.ChatRoomSearchClickListener
import timber.log.Timber
import java.text.NumberFormat
import java.util.Locale

class ChatRoomSearchAdapter(
    private var chatRoomDetail: MutableList<ChatRoomDetailDto>,
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
        val chatRoom = chatRoomDetail[position]
        holder.bind(chatRoom)
    }

    override fun getItemCount(): Int = chatRoomDetail.size

    inner class ChatRoomSearchViewHolder(private val binding: ItemChatRoomSearchBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(chatRoomDetailDto: ChatRoomDetailDto) {
            binding.apply {
                Glide.with(ivThumbnail.context)
                    .load(chatRoomDetailDto.chatRoomThumbnail)
                    .into(ivThumbnail)

                tvTitleChatRoom.text = chatRoomDetailDto.chatRoomTitle

                tvChatRoomTagAge.text = try {
                    Age.valueOf(chatRoomDetailDto.chatRoomAgeRange).displayName
                } catch (e: Exception) {
                    chatRoomDetailDto.chatRoomAgeRange
                }

                tvChatRoomTagStatus.text = try {
                    Job.valueOf(chatRoomDetailDto.chatRoomJob).displayName
                } catch (e: Exception) {
                    chatRoomDetailDto.chatRoomJob
                }

                tvChatRoomTagDays.text = chatRoomDetailDto.chatRoomDDay

                val formattedExpense = NumberFormat.getNumberInstance(Locale.getDefault())
                    .format(chatRoomDetailDto.chatRoomSpendingAmount) + "원"
                tvChatRoomGoalExpense.text = formattedExpense

                tvChatRoomAmountPeople.text = "${chatRoomDetailDto.chatRoomParticipationCount}명"

                tvLock.visibility = if (chatRoomDetailDto.isPassword) View.VISIBLE else View.GONE

                root.setOnClickListener {
                    listener.onItemClicked(chatRoomDetailDto)
                }
            }
        }
    }

    fun updateList(newList: MutableList<ChatRoomDetailDto>) {
        Timber.d("Updating items. Adapter: $this, Items: ${System.identityHashCode(newList)}")
        chatRoomDetail.clear()
        chatRoomDetail.addAll(newList)
        notifyDataSetChanged()
    }
}