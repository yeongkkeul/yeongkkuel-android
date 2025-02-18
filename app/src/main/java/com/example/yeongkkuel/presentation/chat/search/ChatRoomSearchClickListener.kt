package com.example.yeongkkuel.presentation.chat.search

import com.example.yeongkkuel.network.response.chat.ChatRoomDetailDto

interface ChatRoomSearchClickListener {
    fun onItemClicked(chatRoomSearch: ChatRoomDetailDto)
}