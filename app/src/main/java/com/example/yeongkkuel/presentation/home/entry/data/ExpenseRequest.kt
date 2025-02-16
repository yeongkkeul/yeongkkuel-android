package com.example.yeongkkuel.presentation.home.entry.data

import okhttp3.MultipartBody

data class ExpenseRequest(
    val day: String,          // 지출 날짜 (YYYY-MM-DD)
    val categoryId: Int,      // 카테고리 ID
    val content: String,      // 지출 내용
    val amount: Int,          // 지출 금액
    val isExpense: Boolean,   // true면 무지출
    val sendChatRoom: Boolean, // true면 메시지 전송
    val expenseImage: MultipartBody.Part? // 첨부 이미지 (선택 사항)
)

data class ExpenseUpdateRequest(
    val day: String,          // 지출 날짜 (YYYY-MM-DD)
    val categoryId: Int,      // 카테고리 ID
    val content: String,      // 지출 내용
    val amount: Int,          // 지출 금액
    val expenseImage: MultipartBody.Part? // 첨부 이미지 (선택 사항)
)