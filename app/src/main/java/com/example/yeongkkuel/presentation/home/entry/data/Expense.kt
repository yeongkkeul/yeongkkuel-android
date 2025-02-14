package com.example.yeongkkuel.presentation.home.entry.data

data class Expense(
    val noExpense: Boolean,
    val autoSendChat: Boolean,
    val dateInput: String,
    val detailInput: String,
    val amountInput: String,
    val photoUri: String?
)