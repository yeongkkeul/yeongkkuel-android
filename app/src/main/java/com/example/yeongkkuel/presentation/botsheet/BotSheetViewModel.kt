package com.example.yeongkkuel.presentation.botsheet

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
            prev.copy(
                spendingList = updateList
            )
        }
    }

    // 지출 내역 추가 기능
    fun addExpenseToCategory(category: SpendingCategory, history: BotSheetUiState.Spending.History) {
        _uiState.update { prev ->
            val updatedList = prev.spendingList.map { spending ->
                if (spending.kind == category) {
                    spending.copy(
                        history = spending.history + history // 기존 내역에 새 내역 추가
                    )
                } else {
                    spending
                }
            }
            prev.copy(spendingList = updatedList)
        }
    }
    // BotSheetViewModel
    fun addCategory(category: Category) {
        val spendingCategory = mapCategoryToSpendingCategory(category.name)
        val categoryColor = mapCategoryToColor(category.name)
        val plusIconResId = mapCategoryToIcon(categoryColor)

        _uiState.update { prev ->
            val updatedList = prev.spendingList.toMutableList().apply {
                add(
                    BotSheetUiState.Spending(
                        kind = spendingCategory,
                        color = categoryColor,
                        plusIconResId = plusIconResId,
                        history = emptyList()
                    )
                )
            }
            prev.copy(spendingList = updatedList)
        }
    }

    private fun mapCategoryToSpendingCategory(categoryName: String): SpendingCategory {
        return when (categoryName) {
            "간식/음료" -> SpendingCategory.SNACK
            "밥/배달" -> SpendingCategory.SHOP
            "화장품" -> SpendingCategory.BEAUTY
            else -> SpendingCategory.IMPROVEMENT
        }
    }

    private fun mapCategoryToColor(categoryName: String): Colors {
        return when (categoryName) {
            "간식/음료" -> Colors.PINK
            "밥/배달" -> Colors.BLUE
            "화장품" -> Colors.GREEN
            else -> Colors.GREEN
        }
    }

    private fun mapCategoryToIcon(color: Colors): Int {
        return when (color) {
            Colors.PINK -> R.drawable.ic_plus_pink
            Colors.BLUE -> R.drawable.ic_plus_blue
            Colors.GREEN -> R.drawable.ic_plus_green
        }
    }
}
