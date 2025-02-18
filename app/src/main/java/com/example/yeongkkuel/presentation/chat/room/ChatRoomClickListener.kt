package com.example.yeongkkuel.presentation.chat.room

import com.example.yeongkkuel.presentation.chat.ChatRoom

interface ChatRoomClickListener {
    fun onItemDeleted(chatRoom: ChatRoom)
    fun onItemClicked(chatRoom: ChatRoom)
}