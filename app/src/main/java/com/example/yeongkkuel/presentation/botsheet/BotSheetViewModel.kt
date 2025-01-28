package com.example.yeongkkuel.presentation.botsheet

import androidx.core.content.ContextCompat
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

    fun moveCategory(fromPosition: Int, toPosition:Int){
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
        val categoryColor = category.color // 이미 Colors 타입이므로 변환 불필요
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

    private fun mapCategoryToIcon(color: Colors): Int {
        return R.drawable.ic_plus_default // 모든 아이콘은 동일한 XML을 사용
    }
    fun getDayTargetSpending() = viewModelScope.launch {
        try {
            yeongkkuelService.getExpendituresDay().run {
                if(isSuccess){
                    result.run {
                        _uiState.update { prev->
                            prev.copy(
                                targetSpending = dayTargetExpenditure
                            )
                        }
                    }
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

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
                            prev.copy(
                                spendingList = categories.map {
                                    BotSheetUiState.Spending(
                                        kind = SpendingCategory.fromKor(it.categoryName),
                                        color = Colors.fromCode(it.categoryColor) ?: Colors.RED1, // 기본값 추가
                                        plusIconResId = R.drawable.ic_plus_default,
                                        history = it.expenses.map { expense ->
                                            BotSheetUiState.Spending.History(
                                                name = expense.expenseName,
                                                price = expense.expenseAmount
                                            )
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}