package com.example.yeongkkuel.presentation.statsettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class StatSettingsViewModel:ViewModel() {
    private val _uiState = MutableStateFlow(StatSettingsUiState.init())
    val uiState = _uiState.asStateFlow()

    fun setTargetSpending(targetSpending: Int, isSuccess: () -> Unit) = viewModelScope.launch {
        try {
            isSuccess()
        } catch (e:Exception){
            e.printStackTrace()
        }
    }
}