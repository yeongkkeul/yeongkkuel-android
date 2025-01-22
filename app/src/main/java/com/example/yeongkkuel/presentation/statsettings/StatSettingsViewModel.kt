package com.example.yeongkkuel.presentation.statsettings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class StatSettingsViewModel:ViewModel() {
    private val _uiState = MutableStateFlow(StatSettingsUiState.init())
    val uiState = _uiState.asStateFlow()
}