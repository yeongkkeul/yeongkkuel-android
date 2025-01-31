package com.example.yeongkkuel.presentation.botsheet

interface BotSheetListener {
    fun setPeekHeight(peekHeight: Int)
    fun setBotSheetGone()
    fun setBotSheetVisible()
    fun navigateToExpenseEntry(selectedCategory: String, categoryColor: Int)
    fun navigateToExpenseView(expenseName: String, expensePrice: Int, categoryColor: Int)
    fun navigateToCategoryAddFragment()
}