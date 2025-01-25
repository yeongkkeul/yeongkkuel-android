package com.example.yeongkkuel.presentation.home.entry

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ExpenseEntryViewModel : ViewModel() {

    private val _expenseEntries = MutableLiveData<MutableList<EntryData>>(mutableListOf())
    val expenseEntries: LiveData<MutableList<EntryData>> get() = _expenseEntries

    // 새로운 지출 내역 추가
    fun addExpenseEntry(entry: EntryData) {
        val currentList = _expenseEntries.value ?: mutableListOf()
        currentList.add(entry)
        _expenseEntries.value = currentList
    }
}
