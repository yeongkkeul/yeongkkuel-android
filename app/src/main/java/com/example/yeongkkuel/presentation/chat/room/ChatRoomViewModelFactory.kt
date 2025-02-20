package com.example.yeongkkuel.presentation.chat.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.yeongkkuel.presentation.chat.ChatRoomViewModel

class ChatRoomViewModelFactory(private val repository: ChatRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatRoomViewModel::class.java)) {
            return ChatRoomViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(ChatGroupViewModel::class.java)) {
            return ChatGroupViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}