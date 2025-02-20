package com.example.yeongkkuel.presentation.my

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yeongkkuel.network.RetrofitClient.notificationService
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {

    private val _unreadNotification = MutableLiveData<Boolean>()
    val unreadNotification: LiveData<Boolean> get() = _unreadNotification

    fun checkUnreadNotifications() {
        viewModelScope.launch {
            val response = notificationService.getUnreadNotificationCount()
            if (response.isSuccess) {
                Log.d("NotificationViewModel", "checkUnreadNotifications: ${response.result}")
                _unreadNotification.value = response.result ?: false
            } else {
                _unreadNotification.value = false
            }
        }
    }
}