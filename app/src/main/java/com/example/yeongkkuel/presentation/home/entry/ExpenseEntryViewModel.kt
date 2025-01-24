package com.example.yeongkkuel.presentation.home.entry

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ExpenseEntryViewModel : ViewModel() {

    private val _entryData = MutableLiveData<EntryData>()
    val entryData: LiveData<EntryData> get() = _entryData

    fun saveEntryData(data: EntryData) {
        _entryData.value = data
    }

    fun getEntryData(): EntryData? {
        return _entryData.value
    }
}
