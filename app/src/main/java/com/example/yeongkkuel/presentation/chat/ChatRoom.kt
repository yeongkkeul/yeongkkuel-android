package com.example.yeongkkuel.presentation.chat

data class ChatRoom(
    val id: String,
    val title: String,
    val thumbnailUrl: String,
    val recentMessage: String,
    val messageTime: String,
    val participantCount: Int
)