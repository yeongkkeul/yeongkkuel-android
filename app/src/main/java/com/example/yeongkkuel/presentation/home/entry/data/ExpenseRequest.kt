package com.example.yeongkkuel.presentation.home.entry.data

data class ExpenseRequest(
    val day: String,
    val categoryId: Int,
    val content: String,
    val amount: Int,
    val isExpense: Boolean,
    val expenseImg: String,
    val sendChatRoom: Boolean
)