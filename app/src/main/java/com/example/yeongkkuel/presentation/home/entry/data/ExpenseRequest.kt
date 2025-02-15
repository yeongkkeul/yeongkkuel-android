package com.example.yeongkkuel.presentation.home.entry.data

data class ExpenseRequest(
    val day: String,
    val categoryId: Int,
    val content: String,
    val amount: Int,
    val isExpense: Boolean,
    val expenseImg: String?,
    val sendChatRoom: Boolean
)

data class ExpenseUpdateRequest(
    val day: String,       // 지출 날짜 (YYYY-MM-DD)
    val categoryId: Int,   // 카테고리 ID
    val content: String,   // 지출 내용
    val amount: Int,       // 지출 금액
    val expenseImg: String? // 첨부 이미지 (선택 사항)
)
