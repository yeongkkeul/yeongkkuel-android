package com.example.yeongkkuel.presentation.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yeongkkuel.presentation.chat.data.ChatItemModel
import com.example.yeongkkuel.presentation.chat.data.ChatMessage
import com.example.yeongkkuel.presentation.chat.data.ChatRequest
import com.example.yeongkkuel.presentation.chat.network.ChatAPI
import kotlinx.coroutines.launch
import timber.log.Timber

class ChatGroupViewModel : ViewModel() {
    private val _messages = MutableLiveData<MutableList<ChatItemModel>>()
    val messages: LiveData<MutableList<ChatItemModel>> get() = _messages

    val message = MutableLiveData<String>()

    init {
        _messages.value = mutableListOf()
    }

    fun clearChatMessages() {
        _messages.value = mutableListOf()
    }

    fun sendMessage() {
        val currentMessage = message.value ?: return
        Timber.tag("ChatViewModel").d("유저가 전송한 메시지: %s", currentMessage)
        _messages.value?.add(ChatItemModel(sender = "You", content = currentMessage, isUser = true, sendTime = "00:00", amountPeopleRead = 0))
        _messages.postValue(_messages.value)

        val userMessage = ChatMessage(role = "user", content = currentMessage)
        val systemMessage = ChatMessage(role = "system", content = "Say this is a test!")
        val request = ChatRequest(model = "gpt-4o-mini", messages = listOf(systemMessage, userMessage))

        viewModelScope.launch {
            try {
                val response = ChatAPI.retrofitService.sendMessage(request)
                val reply = response.choices.firstOrNull()?.message?.content?.trim() ?: "No response"
                Timber.tag("ChatViewModel").d("GPT가 전달한 메시지: %s", reply)

                _messages.value?.add(ChatItemModel(sender="ChatGPT", content = reply, isUser = false, sendTime = "00:00", amountPeopleRead = 0))
                _messages.postValue(_messages.value)
            } catch (e: Exception) {
                Timber.tag("ChatViewModel").e("에러 메시지: %s", e.message)
                _messages.value?.add(ChatItemModel(sender = "ChatGPT", content = "Error occurred: ${e.message}", isUser = false, sendTime = "00:00", amountPeopleRead = 0))
                _messages.postValue(_messages.value)
            }
        }

        message.value = ""
    }
}