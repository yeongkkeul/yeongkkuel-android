package com.example.yeongkkuel.presentation.botsheet

import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.example.yeongkkuel.R
import com.example.yeongkkuel.presentation.home.category.Category
import com.example.yeongkkuel.presentation.util.Colors
import com.example.yeongkkuel.presentation.util.SpendingCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BotSheetViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<BotSheetUiState>(BotSheetUiState.init())
    val uiState = _uiState.asStateFlow()

    // 카테고리 이동 기능
    fun moveCategory(fromPosition: Int, toPosition: Int) {
        val updateList = uiState.value.spendingList.toMutableList()
        val item = updateList.removeAt(fromPosition)
        updateList.add(toPosition, item)

        _uiState.update { prev ->
            prev.copy(spendingList = updateList)
        }
    }

    // 지출 내역 추가 기능
    fun addExpenseToCategory(category: SpendingCategory, history: BotSheetUiState.Spending.History) {
        _uiState.update { prev ->
            val updatedList = prev.spendingList.map { spending ->
                if (spending.kind == category) {
                    spending.copy(history = spending.history + history) // 기존 내역에 새 내역 추가
                } else {
                    spending
                }
            }
            prev.copy(spendingList = updatedList)
        }
    }

    // 카테고리 추가 기능
    fun addCategory(category: Category) {
        val spendingCategory = SpendingCategory.CUSTOM(category.name)
        val categoryColor = mapCategoryToColor(category.color) // Int 값을 Colors로 변환
        val plusIconResId = mapCategoryToIcon(categoryColor)  // 아이콘도 색상에 맞게 설정

        _uiState.update { prev ->
            val updatedList = prev.spendingList.toMutableList().apply {
                add(
                    BotSheetUiState.Spending(
                        kind = spendingCategory,
                        color = categoryColor,
                        plusIconResId = plusIconResId,
                        history = emptyList() // 초기값으로 빈 리스트
                    )
                )
            }
            prev.copy(spendingList = updatedList)
        }
    }

    // 카테고리 이름을 매핑하는 함수 (이제 입력된 이름을 그대로 사용)
    private fun mapCategoryToSpendingCategory(categoryName: String): SpendingCategory {
        return SpendingCategory.CUSTOM(categoryName)// 모든 카테고리를 기본 CUSTOM으로 설정
    }

    private fun mapCategoryToColor(categoryColor: Int): Colors {
        // 이제 Colors enum으로 변환할 필요 없이 그대로 반환
        return Colors.values().firstOrNull { it.id == categoryColor } ?: Colors.GREEN
    }
    private fun mapCategoryToIcon(color: Colors): Int {
        return when (color) {
            Colors.PINK -> R.drawable.ic_plus_pink
            Colors.BLUE -> R.drawable.ic_plus_blue
            Colors.GREEN -> R.drawable.ic_plus_green
        }
    }
}
