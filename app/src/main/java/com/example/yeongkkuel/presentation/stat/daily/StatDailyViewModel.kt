package com.example.yeongkkuel.presentation.stat.daily

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class StatDailyViewModel: ViewModel() {
    private val _uiState = MutableStateFlow<StatDailyUiState>(StatDailyUiState.init())
    val uiState = _uiState.asStateFlow()
}