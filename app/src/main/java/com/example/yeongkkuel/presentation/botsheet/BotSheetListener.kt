package com.example.yeongkkuel.presentation.botsheet

interface BotSheetListener {
    fun setPeekHeight(peekHeight: Int)
    fun setBotSheetGone()
    fun setBotSheetVisible()
    fun navigateToExpenseEntry(selectedCategory: String, categoryColor: Int)
    fun navigateToExpenseEdit(expenseName: String, expensePrice: Int) // 🔹 추가
    fun navigateToCategoryAddFragment()
}