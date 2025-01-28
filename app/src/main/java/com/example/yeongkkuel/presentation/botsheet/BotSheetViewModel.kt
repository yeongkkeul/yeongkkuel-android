package com.example.yeongkkuel.presentation.botsheet

import androidx.lifecycle.ViewModel
import com.example.yeongkkuel.presentation.util.SpendingCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BotSheetViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BotSheetUiState.init())
    val uiState: StateFlow<BotSheetUiState> = _uiState.asStateFlow()

    // spendingList만 별도로 StateFlow로 관리
    private val _spendingList = MutableStateFlow(_uiState.value.spendingList)
    val spendingList: StateFlow<List<BotSheetUiState.Spending>> = _spendingList.asStateFlow()

    // 카테고리 이동 기능
    fun moveCategory(fromPosition: Int, toPosition: Int) {
        val updatedList = _spendingList.value.toMutableList().apply {
            val item = removeAt(fromPosition)
            add(toPosition, item)
        }
        _spendingList.value = updatedList

        // UI 상태 동기화
        _uiState.update { prev -> prev.copy(spendingList = updatedList) }
    }

    // 지출 내역 추가 기능
    fun addExpenseToCategory(category: SpendingCategory, history: BotSheetUiState.Spending.History) {
        val updatedList = _spendingList.value.map { spending ->
            if (spending.kind == category) {
                // 중복 데이터 방지
                if (spending.history.contains(history)) spending
                else spending.copy(history = spending.history + history)
            } else spending
        }

        _spendingList.value = updatedList

        // UI 상태 동기화
        _uiState.update { prev -> prev.copy(spendingList = updatedList) }
    }
}
