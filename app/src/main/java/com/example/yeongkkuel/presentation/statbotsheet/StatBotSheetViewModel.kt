package com.example.yeongkkuel.presentation.statbotsheet

import androidx.lifecycle.ViewModel
import com.example.yeongkkuel.presentation.stat.daily.StatDailyUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class StatBotSheetViewModel:ViewModel() {
    private val _uiState = MutableStateFlow<StatBotSheetUiState>(StatBotSheetUiState.init())
    val uiState = _uiState.asStateFlow()
}