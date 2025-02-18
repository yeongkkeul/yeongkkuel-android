package com.example.yeongkkuel.presentation.chat.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yeongkkuel.network.RetrofitClient
import com.example.yeongkkuel.network.response.chat.ChatDetailResult
import com.example.yeongkkuel.network.response.chat.ChatRoomDetailDto
import com.example.yeongkkuel.presentation.chat.data.Age
import com.example.yeongkkuel.presentation.chat.data.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class ChatSearchViewModel : ViewModel() {
    val selectedAgeOption: MutableLiveData<String?> = MutableLiveData(null)

    val selectedExpenseOption: MutableLiveData<String> = MutableLiveData()

    val selectedJobOption: MutableLiveData<String?> = MutableLiveData(null)

    private val _chatRoomList = MutableLiveData<MutableList<ChatRoomDetailDto>>()
    val chatRoomList: LiveData<MutableList<ChatRoomDetailDto>> get() = _chatRoomList

    fun fetchChatRooms() {
        viewModelScope.launch {
            try {
                val age: String? = selectedAgeOption.value
                val job: String? = selectedJobOption.value
                val minAmount = 0
                val maxAmount = 1000000
                val page = 0

                val response = RetrofitClient.chatService.getChatroomExplore(age, minAmount, maxAmount, job, page)
                Timber.d("response: $response")
                _chatRoomList.postValue(response.publicChatRoomDetailDtos.toMutableList())
            } catch (e: Exception) {
                // 네트워크 에러 등 예외 처리
                Timber.d("response: $e")
            }
        }
    }

    fun searchChatRooms(keyword: String) {
        viewModelScope.launch {
            try {
                val page = 0
                val response = RetrofitClient.chatService.getChatroomSearch(keyword, page)
                _chatRoomList.postValue(response.publicChatRoomDetailDtos.toMutableList())
            } catch (e: Exception) {
                // 네트워크 에러 등 예외 처리
            }
        }
    }

    fun fetchChatDetail(chatRoomId: Int, onResult: (ChatDetailResult?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.chatService.getChatroomDetail(chatRoomId)
                if (response.isSuccess) {
                    onResult(response.result)
                } else {
                    onResult(null)
                }
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }
}