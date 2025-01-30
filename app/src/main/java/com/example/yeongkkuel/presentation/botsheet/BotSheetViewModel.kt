package com.example.yeongkkuel.presentation.botsheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yeongkkuel.R
import com.example.yeongkkuel.presentation.home.category.Category
import com.example.yeongkkuel.presentation.network.RetrofitClient
import com.example.yeongkkuel.presentation.util.Colors
import com.example.yeongkkuel.presentation.util.SpendingCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar


class BotSheetViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<BotSheetUiState>(BotSheetUiState.init())
    val uiState = _uiState.asStateFlow()

    private val yeongkkuelService = RetrofitClient.yeongkkuelService

    // ✅ LiveData → StateFlow로 일관된 상태 관리
    private val _spendingHistoryList = MutableStateFlow<List<BotSheetUiState.Spending.History>>(emptyList())
    val spendingHistoryList = _spendingHistoryList.asStateFlow()

    // 🔹 카테고리 이동 기능
    fun moveCategory(fromPosition: Int, toPosition: Int) {
        val updateList = uiState.value.spendingList.toMutableList()
        val item = updateList.removeAt(fromPosition)
        updateList.add(toPosition, item)

        _uiState.update { prev ->
            prev.copy(spendingList = updateList)
        }
    }

    //지출 내역 삭제
    fun removeExpense(expenseName: String) {
        viewModelScope.launch {
            _spendingHistoryList.value = _spendingHistoryList.value.filterNot { it.name == expenseName }
        }
    }

    // 🔹 지출 내역 추가 기능
    fun addExpenseToCategory(category: SpendingCategory, history: BotSheetUiState.Spending.History) {
        _uiState.update { prev ->
            val updatedList = prev.spendingList.map { spending ->
                if (spending.kind == category) {
                    spending.copy(history = spending.history + history)
                } else {
                    spending
                }
            }
            prev.copy(spendingList = updatedList)
        }
        updateSpendingHistoryList() // ✅ 추가된 내역 반영
    }

    // 🔹 카테고리 추가 기능
    fun addCategory(category: Category) {
        val spendingCategory = SpendingCategory.CUSTOM(category.name)
        val categoryColor = category.color
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

    private fun mapCategoryToIcon(color: Colors): Int {
        return R.drawable.ic_plus_default
    }

    // 🔹 일일 목표 지출 가져오기
    fun getDayTargetSpending() = viewModelScope.launch {
        try {
            yeongkkuelService.getExpendituresDay().run {
                if (isSuccess) {
                    result.run {
                        _uiState.update { prev ->
                            prev.copy(targetSpending = dayTargetExpenditure)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 🔹 월별 지출 내역 가져오기
    fun getSpendingList() = viewModelScope.launch {
        try {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            yeongkkuelService.getExpendituresMonthCategory(
                year = year,
                month = month,
                day = day
            ).run {
                if (isSuccess) {
                    result.run {
                        _uiState.update { prev ->
                            val updatedSpendingList = categories.map { category ->
                                BotSheetUiState.Spending(
                                    kind = SpendingCategory.fromKor(category.categoryName),
                                    color = Colors.fromCode(category.categoryColor) ?: Colors.RED1,
                                    plusIconResId = R.drawable.ic_plus_default,
                                    history = category.expenses.map { expense ->
                                        BotSheetUiState.Spending.History(
                                            name = expense.expenseName,
                                            price = expense.expenseAmount,
                                            date = expense.expenseDate,
                                            categoryName = category.categoryName,
                                            categoryColor = category.categoryColor,
                                            content = expense.expenseContent,
                                            photoUrl = expense.expensePhotoUrl
                                        )
                                    }
                                )
                            }
                            prev.copy(spendingList = updatedSpendingList)
                        }
                    }
                    updateSpendingHistoryList()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun updateExpense(updatedExpense: BotSheetUiState.Spending.History) {
        _spendingHistoryList.value = _spendingHistoryList.value.map { expense ->
            if (expense.date == updatedExpense.date && expense.name == updatedExpense.name) {
                updatedExpense // 기존 항목을 수정된 값으로 변경
            } else {
                expense
            }
        }
    }

    // ✅ 최신 지출 내역 업데이트 함수 추가
    private fun updateSpendingHistoryList() {
        val historyList = _uiState.value.spendingList.flatMap { it.history }
        _spendingHistoryList.value = historyList.sortedByDescending { it.date } // 최신순 정렬
    }
}
