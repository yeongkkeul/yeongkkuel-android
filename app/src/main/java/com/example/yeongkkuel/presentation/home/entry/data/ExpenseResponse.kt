package com.example.yeongkkuel.presentation.home.entry.data

import com.example.yeongkkuel.presentation.botsheet.BotSheetUiState

data class ExpenseResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: ExpenseResult
)

data class ExpenseResult(
    val createdAt: String,
    val updatedAt: String,
    val id: Int,
    val day: String,
    val content: String,
    val amount: Int,
    val isNoSpending: Boolean,
    val imageUrl: String?,
    val isSend: Boolean
)
data class ExpenseListResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<ExpenseResult>
)

data class ExpenseUpdateResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: ExpenseUpdateResult? // ✅ 서버에서 `null`일 가능성 있음!
)

data class ExpenseUpdateResult(
    val createdAt: String,  // 생성일
    val updatedAt: String,  // 수정일
    val id: Int,            // 지출 ID
    val day: String,        // 날짜
    val content: String,    // 지출 내용
    val amount: Int,        // 지출 금액
    val isNoSpending: Boolean, // 무지출 여부
    val imageUrl: String?,  // 이미지 URL
    val isSend: Boolean     // 자동 전송 여부
)
data class ExpenseDeleteResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: Any? = null // 삭제 요청 시 반환 데이터가 없는 경우 고려
)

fun ExpenseResult.toBotSheetHistory(): BotSheetUiState.Spending.History {
    return BotSheetUiState.Spending.History(
        id = this.id,
        name = this.content,
        price = this.amount,
        imgExist = !this.imageUrl.isNullOrEmpty()
    )
}

fun ExpenseUpdateResult.toBotSheetHistory(): BotSheetUiState.Spending.History {
    return BotSheetUiState.Spending.History(
        id = this.id,
        name = this.content, // ✅ 지출 내용
        price = this.amount, // ✅ 지출 금액
        imgExist = !this.imageUrl.isNullOrEmpty() // ✅ 사진이 있으면 true
    )
}



