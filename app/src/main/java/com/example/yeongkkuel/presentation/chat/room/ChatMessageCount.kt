package com.example.yeongkkuel.presentation.chat.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_message_count")
data class ChatMessageCount(
    @PrimaryKey val chatRoomId: Int,
    val messageCount: Int
)
