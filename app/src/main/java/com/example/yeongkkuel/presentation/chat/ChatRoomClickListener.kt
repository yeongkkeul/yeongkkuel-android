package com.example.yeongkkuel.presentation.chat

interface ChatRoomClickListener {
    fun onItemDeleted(chatRoom: ChatRoom)
    fun onItemClicked(chatRoom: ChatRoom)
}