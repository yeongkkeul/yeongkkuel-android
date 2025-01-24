package com.example.yeongkkuel.presentation.botsheet

import androidx.lifecycle.ViewModel
import com.example.yeongkkuel.presentation.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BotSheetViewModel:ViewModel() {
    private val _uiState = MutableStateFlow<BotSheetUiState>(BotSheetUiState.init())
    val uiState = _uiState.asStateFlow()

    private val yeongkkuelService = RetrofitClient.yeongkkuelService

    fun moveCategory(fromPosition: Int, toPosition:Int){
        val updateList = uiState.value.spendingList.toMutableList()
        val item = updateList.removeAt(fromPosition)
        updateList.add(toPosition, item)

        _uiState.update {prev->
            prev.copy(
                spendingList = updateList
            )
        }
    }
}