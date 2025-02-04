package com.example.yeongkkuel.presentation.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TermsViewModel: ViewModel() {

    val isCheckedService = MutableLiveData(false)
    val isCheckedPrivacy = MutableLiveData(false)
    val isCheckedAge = MutableLiveData(false)
    val isCheckedThirdParty = MutableLiveData(false)


}