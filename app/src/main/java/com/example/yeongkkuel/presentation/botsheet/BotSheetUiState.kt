package com.example.yeongkkuel.presentation.botsheet

import com.example.yeongkkuel.R
import com.example.yeongkkuel.presentation.home.entry.data.ExpenseUpdateRequest
import com.example.yeongkkuel.presentation.util.Colors
import com.example.yeongkkuel.presentation.util.SpendingCategory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BotSheetUiState(
    val targetSpending: Int = 0,
    val spendingList: List<Spending>,
    val date: Date

) {
    data class Spending(
        val categoryId: Int,
        val kind: SpendingCategory,
        val color: Colors,
        val plusIconResId: Int,
        val history: List<History>
    ) {
        data class History(
            val id: Int,
            val name: String,       // 🔹 지출 이름
            val price: Int,         // 🔹 금액
            val imgExist: Boolean   // 🔹 사진 URL 추가
        ) {
            fun toRequest(categoryId: Int, date: Date): ExpenseUpdateRequest {
                return ExpenseUpdateRequest(
                    day = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN).format(date),
                    categoryId = categoryId,
                    content = name,
                    amount = price,
                    expenseImg = if (imgExist) "image_url_placeholder" else null // ✅ 실제 이미지 URL이 있으면 적용
                )
            }
        }
    }

    companion object {
        fun init() = BotSheetUiState(
            spendingList = emptyList(), // 초기 상태는 빈 리스트
            date = Date()
        )
    }
}