package com.example.yeongkkuel.presentation.botsheet

interface BotSheetListener {
    fun setPeekHeight(peekHeight: Int)
    fun setBotSheetGone()
    fun setBotSheetVisible()
    fun navigateToExpenseEntry(selectedCategory: String, categoryColor: Int)
    fun navigateToExpenseView(expenseId: Int, expenseName: String, expensePrice: Int, categoryColor: Int, categoryName: String, imageUrl: Boolean)
    fun navigateToCategoryAddFragment()
    fun onNoExpenseChanged(isNoExpense: Boolean) // 무지출 여부 전달
}