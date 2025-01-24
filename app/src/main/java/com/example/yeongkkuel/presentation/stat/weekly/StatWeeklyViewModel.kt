package com.example.yeongkkuel.presentation.stat.weekly

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class StatWeeklyViewModel:ViewModel() {
    private val _uiState = MutableStateFlow<StatWeeklyUiState>(StatWeeklyUiState.init())
    val uiState = _uiState.asStateFlow()
}