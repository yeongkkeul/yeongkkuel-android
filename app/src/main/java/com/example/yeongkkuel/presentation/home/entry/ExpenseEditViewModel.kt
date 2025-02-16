package com.example.yeongkkuel.presentation.home.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.yeongkkuel.presentation.botsheet.BotSheetViewModel

class ExpenseEditViewModelFactory(
    private val viewModel: BotSheetViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BotSheetViewModel::class.java)) {
            return viewModel as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
