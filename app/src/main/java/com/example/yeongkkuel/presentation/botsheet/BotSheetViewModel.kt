package com.example.yeongkkuel.presentation.botsheet

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yeongkkuel.R
import com.example.yeongkkuel.presentation.home.category.Category
import com.example.yeongkkuel.presentation.network.RetrofitClient
import com.example.yeongkkuel.presentation.util.Colors
import com.example.yeongkkuel.presentation.util.SpendingCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Calendar


class BotSheetViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BotSheetUiState.init())
    val uiState = _uiState.asStateFlow()

    private val yeongkkuelService = RetrofitClient.yeongkkuelService

    // ✅ LiveData → StateFlow로 일관된 상태 관리
    private val _spendingHistoryList = MutableStateFlow<List<BotSheetUiState.Spending.History>>(emptyList())
    val spendingHistoryList = _spendingHistoryList.asStateFlow()

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
    private val mutex = Mutex()

    suspend fun updateBotSheetHistory(updatedExpense: BotSheetUiState.Spending.History) {
        Log.d("BotSheetViewModel", "🔄 updateBotSheetHistory() 실행됨: $updatedExpense")

        mutex.withLock {
            _uiState.update { prevState ->
                val originalSpendingList = prevState.spendingList.toMutableList()

                var previousCategory: BotSheetUiState.Spending? = null
                var updatedCategory: BotSheetUiState.Spending? = null

                // 1️⃣ 기존 리스트에서 삭제 & 새로운 리스트에 추가
                val updatedSpendingList = originalSpendingList.map { spending ->
                    if (spending.kind.kor == updatedExpense.categoryName) {
                        // ✅ 기존 내역 삭제 (이전 날짜에서 제거)
                        val filteredHistory = spending.history.filterNot { it.name == updatedExpense.name && it.date == updatedExpense.date }

                        previousCategory = spending.copy(history = filteredHistory)

                        spending.copy(history = filteredHistory)
                    } else {
                        spending
                    }
                }.toMutableList()

                // ✅ 새로운 날짜에 추가
                val existingCategory = updatedSpendingList.find { it.kind.kor == updatedExpense.categoryName }
                if (existingCategory != null) {
                    updatedCategory = existingCategory.copy(history = existingCategory.history + updatedExpense)
                    updatedSpendingList.remove(existingCategory)
                    updatedSpendingList.add(updatedCategory)
                } else {
                    updatedCategory = BotSheetUiState.Spending(
                        kind = SpendingCategory.fromKor(updatedExpense.categoryName),
                        color = Colors.fromCode(updatedExpense.categoryColor) ?: Colors.RED1,
                        plusIconResId = R.drawable.ic_plus_default,
                        history = listOf(updatedExpense)
                    )
                    updatedSpendingList.add(updatedCategory)
                }

                Log.d("BotSheetViewModel", "✅ 기존 내역 제거됨: $previousCategory")
                Log.d("BotSheetViewModel", "✅ 새로운 내역 추가됨: $updatedCategory")

                prevState.copy(spendingList = updatedSpendingList)
            }

            updateSpendingHistoryList() // ✅ 최신 내역 반영
        }
    }

    private fun updateSpendingHistoryList() {
        val historyList = _uiState.value.spendingList.flatMap { it.history }

        Log.d("BotSheetViewModel", "📌 updateSpendingHistoryList() 실행됨")
        Log.d("BotSheetViewModel", "📌 최신 spendingHistoryList: $historyList") // ✅ 최신 리스트 확인

        _spendingHistoryList.value = historyList.sortedByDescending { it.date }
    }

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
        _uiState.update { prev ->
            val updatedList = prev.spendingList.map { spending ->
                spending.copy(history = spending.history.filterNot { it.name == expenseName })
            }.filterNot { it.history.isEmpty() } // 🔥 내역이 비어 있으면 해당 카테고리 제거

            prev.copy(spendingList = updatedList)
        }

        // ✅ 최신 내역 업데이트
        updateSpendingHistoryList()

        Log.d("BotSheetViewModel", "📌 삭제됨: $expenseName, 남은 지출 개수: ${_spendingHistoryList.value.size}")
    }

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

        Log.d("BotSheetViewModel", "✅ 카테고리 추가됨: ${category.name}")

    }


    private fun mapCategoryToIcon(color: Colors): Int {
        return R.drawable.ic_plus_default
    }

    // 카테고리 제목, 색상 수정 후 바텀시트 업로드
    fun updateCategory(originalCategoryName: String, updatedCategory: Category) {
        val updatedCategoryColor = updatedCategory.color // ✅ Colors 타입 유지

        val updatedSpendingList = uiState.value.spendingList.map { spending ->
            if (spending.kind.kor == originalCategoryName) {
                spending.copy(
                    kind = SpendingCategory.fromName(updatedCategory.name),
                    color = updatedCategoryColor // ✅ Colors 타입 유지
                )
            } else {
                spending
            }
        }

        // ✅ 기존 지출 내역의 categoryColor도 Colors 타입 유지
        _spendingHistoryList.value = _spendingHistoryList.value.map { history ->
            if (history.categoryName == originalCategoryName) {
                history.copy(categoryColor = updatedCategoryColor.toString()) // ✅ String 변환
            } else {
                history
            }
        }

        _uiState.update { prevState ->
            prevState.copy(spendingList = updatedSpendingList)
        }
    }

    // 카테고리 삭제 연동 기능
    fun removeCategory(categoryName: String) {
        _uiState.update { prev ->
            val updatedSpendingList = prev.spendingList.filter { it.kind.kor != categoryName }
            prev.copy(spendingList = updatedSpendingList)
        }
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
    fun getCategoryList(): List<Category> {
        val categoryList = _uiState.value.spendingList.map { spending ->
            Category(name = spending.kind.kor, color = spending.color)
        }
        Log.d("BotSheetViewModel", "📌 getCategoryList() 반환: $categoryList") // 🔥 최신 카테고리 확인
        return categoryList
    }

}