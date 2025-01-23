package com.example.yeongkkuel.presentation.botsheet

interface BotSheetListener {
    fun setPeekHeight(peekHeight: Int)

    fun setBotSheetGone()
    fun setBotSheetVisible()
    fun navigateToExpenseEntry()
}