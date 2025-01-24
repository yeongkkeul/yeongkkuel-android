package com.example.yeongkkuel.presentation.botsheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yeongkkuel.presentation.network.RetrofitClient
import com.example.yeongkkuel.presentation.util.Colors
import com.example.yeongkkuel.presentation.util.SpendingCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

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
                if(isSuccess){
                    result.run{
                        _uiState.update { prev->
                            prev.copy(
                                spendingList = categories.map {
                                    BotSheetUiState.Spending(
                                        kind = SpendingCategory.fromKor(it.categoryName),
                                        color = Colors.fromCode(it.categoryColor),
                                        history = it.expenses.map {
                                            BotSheetUiState.Spending.History(
                                                name = it.expenseName,
                                                price = it.expenseAmount
                                            )
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}