package com.example.yeongkkuel.presentation.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yeongkkuel.network.RetrofitClient
import kotlinx.coroutines.launch
import timber.log.Timber

class ChatRoomViewModel : ViewModel() {

    private val _chatRooms = MutableLiveData<List<ChatRoom>>()
    val chatRooms: LiveData<List<ChatRoom>> = _chatRooms

    init {
        fetchChatRooms()
    }

    fun fetchChatRooms() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.chatService.getChatList()
                if (response.isSuccess) {
                    response.result.let { result ->
                        // API 응답 결과를 기존 ChatRoom 모델로 매핑
                        val rooms = result.map { dto ->
                            ChatRoom(
                                id = dto.chatRoomId,
                                title = dto.chatRoomTitle,
                                thumbnailUrl = dto.chatRoomThumbnail,
                                recentMessage = "", // API에 해당 값이 없다면 기본값 사용
                                messageTime = "",
                                participantCount = dto.participationCount,
                                chatRoomRule = dto.chatRoomRule
                            )
                        }
                        _chatRooms.value = rooms
                    }
                } else {
                    // 에러 처리: 로그 출력 또는 에러 LiveData 갱신 등
                    Timber.e("Error fetching chat rooms: ${response.code}")
                }
            } catch (e: Exception) {
                // 네트워크 에러 등 예외 처리
                e.printStackTrace()
            }
        }
    }
}