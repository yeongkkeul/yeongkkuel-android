package com.example.yeongkkuel.presentation.chat.data

data class ChatItemModel(
    val sender: String,
    val content: String,
    val isUser: Boolean,
    val sendTime: String,
    val amountPeopleRead: Int
)
