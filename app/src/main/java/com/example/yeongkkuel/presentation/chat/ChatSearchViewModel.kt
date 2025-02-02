package com.example.yeongkkuel.presentation.chat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChatSearchViewModel : ViewModel() {
    val selectedAgeOption: MutableLiveData<String> = MutableLiveData()

    val selectedExpenseOption: MutableLiveData<String> = MutableLiveData()

    val selectedJobOption: MutableLiveData<String> = MutableLiveData()
}