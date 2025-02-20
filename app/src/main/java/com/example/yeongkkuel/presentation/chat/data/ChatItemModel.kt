package com.example.yeongkkuel.presentation.chat.data

data class ChatItemModel(
    val sender: String,
    val content: String,
    val isUser: Boolean,
    val sendTime: String,
    val amountPeopleRead: Int,
    val profileImageUrl: String?,
    val receiptCategory: String? = null,
    val receiptContent: String? = null,
    val receiptAmount: Int? = null
)
