package com.example.yeongkkuel.presentation.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.ItemChatOtherBinding
import com.example.yeongkkuel.databinding.ItemChatUserBinding
import com.example.yeongkkuel.presentation.chat.ChatMessageClickListener
import com.example.yeongkkuel.presentation.chat.data.ChatItemModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatGroupAdapter(
    private val messages: List<ChatItemModel>,
    private val otherProfileImageUrl: String?,
    private val chatMessageClickListener: ChatMessageClickListener
) : RecyclerView.Adapter<ChatGroupAdapter.ChatViewHolder>() {

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_OTHER = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) {
            VIEW_TYPE_USER // 작성자 메시지
        } else {
            VIEW_TYPE_OTHER // 상대방 메시지
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = when (viewType) {
            VIEW_TYPE_USER -> ItemChatUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            VIEW_TYPE_OTHER -> ItemChatOtherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            else -> throw IllegalArgumentException("Invalid view type")
        }
        return ChatViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(messages[position], otherProfileImageUrl)

        val context = holder.itemView.context
        val animation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        holder.itemView.startAnimation(animation)
    }

    override fun getItemCount(): Int = messages.size

    val currentTime: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    inner class ChatViewHolder(private val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chatMessage: ChatItemModel, otherProfileImageUrl: String?) {
            when (binding) {
                is ItemChatUserBinding -> {
                    binding.tvChatMessage.text = chatMessage.content
                    binding.tvTimeMessage.text = currentTime
                    binding.tvAmountPeopleRead.text = chatMessage.amountPeopleRead.toString()
                }
                is ItemChatOtherBinding -> {
                    binding.tvNicknameSender.text = chatMessage.sender
                    binding.tvTimeMessage.text = currentTime
                    binding.tvAmountPeopleRead.text = chatMessage.amountPeopleRead.toString()
                    binding.tvChatMessage.text = chatMessage.content
                    binding.ivProfileSender.setOnClickListener {
                        chatMessageClickListener.onMessageClicked()
                    }
                    otherProfileImageUrl?.let {
                        Glide.with(binding.ivProfileSender.context)
                            .load(it)
                            .circleCrop()
                            .placeholder(R.drawable.ic_logo) // 기본 이미지
                            .error(R.drawable.ic_logo) // 에러 이미지
                            .into(binding.ivProfileSender)
                    }
                }
            }
        }
    }
}