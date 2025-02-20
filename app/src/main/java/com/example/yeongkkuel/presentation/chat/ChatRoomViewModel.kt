package com.example.yeongkkuel.presentation.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yeongkkuel.network.RetrofitClient
import com.example.yeongkkuel.presentation.chat.room.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.Locale

class ChatRoomViewModel(private val repository: ChatRepository) : ViewModel() {

    private val _chatRooms = MutableLiveData<List<ChatRoom>>()
    val chatRooms: LiveData<List<ChatRoom>> = _chatRooms

    private var pollingJob: Job? = null

    init {
        fetchChatRooms()
        startPollingChatRooms()
    }

    // 일정 간격마다 채팅방 목록과 최신 메시지를 업데이트하는 폴링 함수
    private fun startPollingChatRooms() {
        pollingJob = viewModelScope.launch {
            while (isActive) {
                fetchChatRooms()
                delay(5000L) // 3초마다 업데이트 (필요에 따라 간격 조정)
            }
        }
    }

    // 화면을 벗어나면 polling 중단
    fun stopPollingChatRooms() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun fetchChatRooms() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.chatService.getChatList()
                if (response.isSuccess) {
                    response.result.let { result ->
                        // API 응답 결과를 ChatRoom 모델로 매핑 (초기 unreadCount는 0)
                        val rooms = result.map { dto ->
                            ChatRoom(
                                id = dto.chatRoomId,
                                title = dto.chatRoomTitle,
                                thumbnailUrl = dto.chatRoomThumbnail ?: "",
                                recentMessage = "",
                                messageTime = "",
                                participantCount = dto.participationCount,
                                chatRoomRule = dto.chatRoomRule,
                                unreadCount = 0
                            )
                        }
                        _chatRooms.value = rooms

                        // 각 채팅방별로 최신 메시지와 전체 메시지 개수 업데이트
                        rooms.forEach { room ->
                            fetchLatestTextMessageForChatRoom(room.id)
                        }
                    }
                } else {
                    Timber.e("Error fetching chat rooms: ${response.code}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun fetchLatestTextMessageForChatRoom(chatRoomId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.chatService.getChatroomMessageTest(chatRoomId)
                if (response.isSuccess) {
                    val messages = response.result

                    // 총 메시지 개수 (전체 메시지 목록 크기)
                    val totalCount = messages.size

                    // Room DB에 저장된 마지막 읽은 메시지 개수 조회
                    val storedCount = repository.getMessageCount(chatRoomId)?.messageCount ?: 0

                    // 안 읽은 메시지 수 계산
                    val unreadCount = totalCount - storedCount

                    // UI 업데이트: unreadCount 반영
                    updateChatRoomUnreadCount(chatRoomId, unreadCount)

                    // 최신 메시지 업데이트 처리
                    val latestMessage = messages.maxByOrNull { it.timestamp }
                    latestMessage?.let { message ->
                        if (message.messageType == "RECEIPT") {
                            val expenseId = message.content.toIntOrNull()
                            if (expenseId != null) {
                                try {
                                    val receiptResponse = RetrofitClient.chatService.getReceiptsByExpendsId(expenseId)
                                    if (receiptResponse.isSuccess) {
                                        val receipt = receiptResponse.result
                                        // 예: "ㅎ의 영수증 도착!" 형태
                                        val displayText = "${receipt.senderName}의 영수증 도착!"
                                        updateChatRoomRecentMessage(chatRoomId, displayText, message.timestamp)
                                    } else {
                                        updateChatRoomRecentMessage(chatRoomId, message.content, message.timestamp)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    updateChatRoomRecentMessage(chatRoomId, message.content, message.timestamp)
                                }
                            } else {
                                updateChatRoomRecentMessage(chatRoomId, message.content, message.timestamp)
                            }
                        } else {
                            updateChatRoomRecentMessage(chatRoomId, message.content, message.timestamp)
                        }
                    }
                } else {
                    Timber.e("Error fetching messages for chatRoomId $chatRoomId: ${response.code}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val updateMutex = Mutex()

    private fun updateChatRoomRecentMessage(chatRoomId: Int, content: String, timestamp: String) {
        viewModelScope.launch {
            updateMutex.withLock {
                // timestamp를 원하는 형식으로 변환
                val formattedTime = formatTimestamp(timestamp)
                Timber.d("updateChatRoomRecentMessage: $formattedTime")

                // 기존 채팅방 목록을 업데이트 (LiveData에 반영)
                _chatRooms.value?.let { rooms ->
                    val updatedRooms = rooms.map { room ->
                        if (room.id == chatRoomId) {
                            room.copy(recentMessage = content, messageTime = formattedTime)
                        } else {
                            room
                        }
                    }
                    _chatRooms.value = updatedRooms // setValue 사용 (메인 스레드에서 바로 업데이트)
                }
            }
        }
    }

    // unreadCount 업데이트 함수
    private fun updateChatRoomUnreadCount(chatRoomId: Int, unreadCount: Int) {
        viewModelScope.launch {
            updateMutex.withLock {
                _chatRooms.value?.let { rooms ->
                    val updatedRooms = rooms.map { room ->
                        if (room.id == chatRoomId) {
                            room.copy(unreadCount = unreadCount)
                        } else {
                            room
                        }
                    }
                    _chatRooms.value = updatedRooms
                }
            }
        }
    }

    private fun formatTimestamp(timestamp: String): String {
        return try {
            // 입력: API에서 전달하는 ISO-8601 형식의 timestamp (예: "2025-02-20T21:20:08.251923139")
            val inputFormatter = DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                .toFormatter(Locale.getDefault())
            val dateTime = LocalDateTime.parse(timestamp, inputFormatter)
            val messageDate: LocalDate = dateTime.toLocalDate()
            val today: LocalDate = LocalDate.now()
            val yesterday: LocalDate = today.minusDays(1)

            when (messageDate) {
                // 메시지가 오늘인 경우: "오전/오후 hh:mm" 형식
                today -> {
                    val outputFormatter = DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREAN)
                    dateTime.format(outputFormatter)
                }
                // 메시지가 어제인 경우: "어제" 텍스트 반환
                yesterday -> {
                    "어제"
                }
                // 그 외의 날짜: "M월 d일" 형식 (예: 12월 22일)
                else -> {
                    val outputFormatter = DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN)
                    dateTime.format(outputFormatter)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 변환에 실패하면 원본 문자열을 반환
            timestamp
        }
    }
}