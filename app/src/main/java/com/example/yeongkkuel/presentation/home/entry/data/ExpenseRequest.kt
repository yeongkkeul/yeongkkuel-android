package com.example.yeongkkuel.presentation.home.entry.data

data class ExpenseRequest(
    val day: String,          // 지출 날짜 (YYYY-MM-DD)
    val categoryId: Int,      // 카테고리 ID
    val content: String,      // 지출 내용
    val amount: Int,          // 지출 금액
    val isExpense: Boolean,   // true면 무지출
    val sendChatRoom: Boolean // true면 메시지 전송
)

data class ExpenseUpdateRequest(
    val day: String,          // 지출 날짜 (YYYY-MM-DD)
    val categoryId: Int,      // 카테고리 ID
    val content: String,      // 지출 내용
    val amount: Int         // 지출 금액
)