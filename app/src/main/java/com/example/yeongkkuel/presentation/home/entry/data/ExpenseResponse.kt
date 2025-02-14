package com.example.yeongkkuel.presentation.home.entry.data

data class ExpenseResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: ExpenseResult
)

data class ExpenseResult(
    val id: Int,
    val day: String,
    val content: String,
    val amount: Int,
    val isNoSpending: Boolean,
    val imageUrl: String,
    val isSend: Boolean
)

data class ExpenseListResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<ExpenseResult>
)
