package com.example.yeongkkuel.presentation.chat.room

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yeongkkuel.network.RetrofitClient
import com.example.yeongkkuel.network.StompClient
import com.example.yeongkkuel.network.response.chat.ChatBannerResult
import com.example.yeongkkuel.network.response.chat.ChatRoomRank
import com.example.yeongkkuel.network.response.chat.ChatRoomUserResult
import com.example.yeongkkuel.presentation.chat.data.ChatItemModel
import com.example.yeongkkuel.presentation.chat.data.ChatMessage
import com.example.yeongkkuel.presentation.chat.data.ChatRequest
import com.example.yeongkkuel.presentation.chat.network.ChatAPI
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber
import ua.naiksoftware.stomp.dto.LifecycleEvent
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.Date
import java.util.Locale

class ChatGroupViewModel(private val repository: ChatRepository) : ViewModel() {
    private val _messages = MutableLiveData<MutableList<ChatItemModel>>()
    val messages: LiveData<MutableList<ChatItemModel>> get() = _messages

    private val _selectedChatRoomId = MutableLiveData<Int?>()
    val selectedChatRoomId: LiveData<Int?> get() = _selectedChatRoomId

    private val _senderId = MutableLiveData<Int?>()
    val senderId: LiveData<Int?> get() = _senderId

    private val _selectedChatRoomPassword = MutableLiveData<String?>()
    val selectedChatRoomPassword: LiveData<String?> get() = _selectedChatRoomPassword

    fun setSelectedChatRoomId(id: Int) {
        _selectedChatRoomId.value = id
    }

    fun setSelectedChatRoomPassword(password: String) {
        _selectedChatRoomPassword.value = password
    }

    fun setSenderId(id: Int) {
        _senderId.value = id
    }

    val message = MutableLiveData<String>()

    private var pollingJob: Job? = null

    init {
        _messages.value = mutableListOf()
        _senderId.value?.let { _selectedChatRoomId.value?.let { it1 ->
            fetchLatestTextMessageForChatRoom(
                it1, it)
        } }
        startPollingChatRooms()
    }

    // 일정 간격마다 채팅방 목록과 최신 메시지를 업데이트하는 폴링 함수
    private fun startPollingChatRooms() {
        pollingJob = viewModelScope.launch {
            while (isActive) {
                _senderId.value?.let { _selectedChatRoomId.value?.let { it1 ->
                    fetchLatestTextMessageForChatRoom(
                        it1, it)
                } }
                delay(2000L) // 2초마다 업데이트 (필요에 따라 간격 조정)
            }
        }
    }

    // 화면을 벗어나면 polling 중단
    fun stopPollingChatRooms() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun fetchLatestTextMessageForChatRoom(chatRoomId: Int, senderId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.chatService.getChatroomMessageTest(chatRoomId)
                if (response.isSuccess) {
                    val messages = response.result
                    // 총 메시지 개수를 Room DB에 저장
                    updateMessageCountInRoomDB(chatRoomId, messages.size)

                    // 각 메시지에 대해 ChatItemModel 생성
                    val list = mutableListOf<ChatItemModel>()
                    for (msg in messages) {
                        if (msg.messageType == "RECEIPT") {
                            // 메시지의 content를 expenseId로 변환
                            val expenseId = msg.content.toIntOrNull()
                            if (expenseId != null) {
                                try {
                                    val receiptResponse = RetrofitClient.chatService.getReceiptsByExpendsId(expenseId)
                                    if (receiptResponse.isSuccess) {
                                        val receipt = receiptResponse.result
                                        list.add(
                                            ChatItemModel(
                                                sender = if (msg.senderId == senderId) "You" else receipt.senderName,
                                                content = msg.content,
                                                isUser = (msg.senderId == senderId),
                                                sendTime = formatSendTime(msg.timestamp),
                                                amountPeopleRead = msg.unreadCount,
                                                profileImageUrl = receipt.imageUrl,
                                                receiptCategory = receipt.category,
                                                receiptContent = receipt.content,
                                                receiptAmount = receipt.amount
                                            )
                                        )
                                    } else {
                                        // Receipt API 호출 실패 시 기본값 사용
                                        list.add(
                                            ChatItemModel(
                                                sender = if (msg.senderId == senderId) "You" else msg.senderId.toString(),
                                                content = msg.content,
                                                isUser = (msg.senderId == senderId),
                                                sendTime = formatSendTime(msg.timestamp),
                                                amountPeopleRead = msg.unreadCount,
                                                profileImageUrl = ""
                                            )
                                        )
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    list.add(
                                        ChatItemModel(
                                            sender = if (msg.senderId == senderId) "You" else msg.senderId.toString(),
                                            content = msg.content,
                                            isUser = (msg.senderId == senderId),
                                            sendTime = formatSendTime(msg.timestamp),
                                            amountPeopleRead = msg.unreadCount,
                                            profileImageUrl = ""
                                        )
                                    )
                                }
                            } else {
                                // expenseId 변환 실패 시 그냥 기본 메시지로 처리
                                list.add(
                                    ChatItemModel(
                                        sender = if (msg.senderId == senderId) "You" else msg.senderId.toString(),
                                        content = msg.content,
                                        isUser = (msg.senderId == senderId),
                                        sendTime = formatSendTime(msg.timestamp),
                                        amountPeopleRead = msg.unreadCount,
                                        profileImageUrl = ""
                                    )
                                )
                            }
                        } else {
                            // RECEIPT 타입이 아닌 경우
                            list.add(
                                ChatItemModel(
                                    sender = if (msg.senderId == senderId) "You" else msg.senderId.toString(),
                                    content = msg.content,
                                    isUser = (msg.senderId == senderId),
                                    sendTime = formatSendTime(msg.timestamp),
                                    amountPeopleRead = msg.unreadCount,
                                    profileImageUrl = ""
                                )
                            )
                        }
                    }
                    _messages.value = list.toMutableList()
                } else {
                    Timber.e("Error fetching messages for chatRoomId $chatRoomId: ${response.code}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun updateMessageCountInRoomDB(chatRoomId: Int, count: Int) {
        viewModelScope.launch {
            repository.saveMessageCount(chatRoomId, count)
        }
    }

    private val stompClient = StompClient.getStompClient()
    private val compositeDisposable = CompositeDisposable()

    fun enterChatRoom(senderId: Int) {
        // 선택된 채팅방 업데이트
        _selectedChatRoomId.value?.let { setSelectedChatRoomId(it) }

        // 채팅방 가입 (필요시)
        _selectedChatRoomId.value?.let { joinChatRoom( it.toLong(), senderId.toLong(), _selectedChatRoomPassword.value) }

        // 해당 채팅방의 메시지 수신 구독
        _selectedChatRoomId.value?.let { setupMessageReceiver( it.toLong()) }
    }

    fun enterChatRoomRegistered() {
        // 해당 채팅방의 메시지 수신 구독
        _selectedChatRoomId.value?.let { setupMessageReceiver( it.toLong()) }
    }

    fun setupStompClient() {
        // STOMP 클라이언트의 생명주기 이벤트 구독 (RxJava 사용)
        val lifecycleDisposable = stompClient.lifecycle()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { lifecycleEvent ->
                when (lifecycleEvent.type) {
                    LifecycleEvent.Type.OPENED -> {
                        Timber.tag("STOMP").d("연결 성공: %s", lifecycleEvent)
                    }
                    LifecycleEvent.Type.ERROR -> {
                        Timber.tag("STOMP").e(lifecycleEvent.exception, "연결 에러: ")
                    }
                    LifecycleEvent.Type.CLOSED -> {
                        Timber.tag("STOMP").d("연결 종료됨. 3초 후 재연결 시도...")
                        Handler(Looper.getMainLooper()).postDelayed({
                            stompClient.connect()
                        }, 3000)  // 3초 후 재연결 시도
                    }
                    else -> {}
                }
            }
        compositeDisposable.add(lifecycleDisposable)

        // 웹소켓 연결 시작
        stompClient.connect()
    }

    fun setupMessageReceiver(chatRoomId: Long) {

        // 수신할 채팅방 메시지 구독
        val destination = "/exchange/chat.exchange/chat.room.$chatRoomId"

        val messageDisposable = stompClient.topic(destination)
            .map { it.payload as String } // 메시지 payload를 String 형식으로 변환
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ payload ->
                try {
                    // 수신된 메시지 JSON 파싱
                    val jsonMessage = JSONObject(payload)
                    val messageId = jsonMessage.getLong("id")
                    val roomId = jsonMessage.getLong("chatRoomId")
                    val senderId = jsonMessage.getLong("senderId")
                    val messageType = jsonMessage.getString("messageType")
                    val content = jsonMessage.getString("content")
                    val timestamp = jsonMessage.getString("timestamp")

                    // 수신된 메시지 로깅
                    Timber.tag("STOMP").d("메시지 수신: id=%d, chatRoomId=%d, senderId=%d, messageType=%s, content=%s, timestamp=%s",
                        messageId, roomId, senderId, messageType, content, timestamp)

                } catch (e: Exception) {
                    Timber.e("STOMP", "수신 메시지 파싱 오류: ${e.message}")
                }
            }, { error ->
                Timber.e("STOMP", "채팅 메시지 수신 실패: ${error.message}")
            })

        compositeDisposable.add(messageDisposable)
    }

    fun sendMessageToChatRoom(chatRoomId: Long, senderId: Long, content: String) {
        // 메시지 송신을 위한 URL (roomId 자리에 chatRoomId 값 삽입)
        val destination = "/pub/chat.message.$chatRoomId"

        // JSON 메시지 구성
        val jsonMessage = JSONObject().apply {
            put("chatRoomId", chatRoomId)
            put("senderId", senderId)
            put("messageType", "TEXT")  // 메시지 유형을 TEXT로 설정
            put("content", content)     // 실제 메시지 내용
        }

        Timber.d("전송할 메시지: $jsonMessage")

        // 메시지 publish (RxJava Observable 구독)
        val sendDisposable = stompClient.send(destination, jsonMessage.toString())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.d("STOMP", "채팅방 메시지 전송 성공")
            }, { error ->
                Timber.e("STOMP", "채팅방 메시지 전송 실패: ${error.message}")
            })

        compositeDisposable.add(sendDisposable)
    }

    private fun joinChatRoom(chatRoomId: Long, senderId: Long, password: String?) {
        // 채팅방 가입 URL 구성 (roomId 자리에 chatRoomId 값 삽입)
        val destination = "/pub/chat.enter.$chatRoomId"

        // JSON 메시지 구성 (content는 빈 문자열로 설정)
        val jsonMessage = JSONObject().apply {
            put("chatRoomId", chatRoomId)
            put("senderId", senderId)
            put("messageType", "ENTER")
            put("content", "ENTER")
            // password가 null인 경우 JSON_NULL로 명시
            put("password", password ?: JSONObject.NULL)
        }

        Timber.d("$jsonMessage")

        // 메시지 publish (RxJava Observable 구독)
        val sendDisposable = stompClient.send(destination, jsonMessage.toString())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.d("STOMP", "채팅방 가입 메시지 전송 성공")
            }, { error ->
                Timber.e("STOMP", "채팅방 가입 메시지 전송 실패: ${error.message}")
            })

        compositeDisposable.add(sendDisposable)
    }

    private val _bannerData = MutableLiveData<ChatBannerResult>()
    val bannerData: LiveData<ChatBannerResult> get() = _bannerData

    fun fetchBanner(chatRoomId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.chatService.getChatroomBanner(chatRoomId)
                if (response.isSuccess) {
                    response.result.let {
                        _bannerData.value = it
                    }
                } else {
                    // 에러 처리 (예: 로그 출력, 에러 LiveData 갱신 등)
                }
            } catch (e: Exception) {
                // 네트워크 에러 등 예외 처리
                e.printStackTrace()
            }
        }
    }

    private val _chatRoomRanks = MutableLiveData<List<ChatRoomRank>>()
    val chatRoomRanks: LiveData<List<ChatRoomRank>> = _chatRoomRanks

    fun getChatRoomRanks(chatRoomId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.chatService.getChatroomRanks(chatRoomId)
                if (response.isSuccess) {
                    // 응답의 result 내부에서 userRanks 리스트를 추출하여 업데이트
                    _chatRoomRanks.value = response.result.userRanks
                } else {
                    // 응답 실패 시 처리 (예: 에러 메시지 로그 출력 등)
                    _chatRoomRanks.value = emptyList()
                }
            } catch (e: Exception) {
                // 네트워크 오류 등 예외 처리
                _chatRoomRanks.value = emptyList()
            }
        }
    }

    fun fetchChatroomUser(chatRoomId: Int, userId: Int, onResult: (ChatRoomUserResult?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.chatService.getChatroomUser(chatRoomId, userId)
                if (response.isSuccess) {
                    onResult(response.result) // result에 사용자 정보가 담겨있다고 가정
                } else {
                    onResult(null)
                }
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }

    fun clearChatMessages() {
        _messages.value = mutableListOf()
    }

    private fun formatSendTime(timestamp: String): String {
        // 예시: API timestamp가 "yyyy-MM-dd'T'HH:mm:ss" 형식으로 온다고 가정하고 "HH:mm"으로 변환
        val inputFormat = DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
            .toFormatter(Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return try {
            val dateTime = LocalDateTime.parse(timestamp, inputFormat)
            val outputFormatter = DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREAN)
            dateTime.format(outputFormatter)
        } catch (e: Exception) {
            e.printStackTrace()
            outputFormat.format(Date())
        }
    }

    fun sendMessage() {
//        val currentMessage = message.value ?: return
//        Timber.tag("ChatViewModel").d("유저가 전송한 메시지: %s", currentMessage)
//        _messages.value?.add(ChatItemModel(sender = "You", content = currentMessage, isUser = true, sendTime = "00:00", amountPeopleRead = 0))
//        _messages.postValue(_messages.value)
//
//        val userMessage = ChatMessage(role = "user", content = currentMessage)
//        val systemMessage = ChatMessage(role = "system", content = "Say this is a test!")
//        val request = ChatRequest(model = "gpt-4o-mini", messages = listOf(systemMessage, userMessage))
//
//        viewModelScope.launch {
//            try {
//                val response = ChatAPI.retrofitService.sendMessage(request)
//                val reply = response.choices.firstOrNull()?.message?.content?.trim() ?: "No response"
//                Timber.tag("ChatViewModel").d("GPT가 전달한 메시지: %s", reply)
//
//                _messages.value?.add(ChatItemModel(sender="ChatGPT", content = reply, isUser = false, sendTime = "00:00", amountPeopleRead = 0))
//                _messages.postValue(_messages.value)
//            } catch (e: Exception) {
//                Timber.tag("ChatViewModel").e("에러 메시지: %s", e.message)
//                _messages.value?.add(ChatItemModel(sender = "ChatGPT", content = "Error occurred: ${e.message}", isUser = false, sendTime = "00:00", amountPeopleRead = 0))
//                _messages.postValue(_messages.value)
//            }
//        }

        message.value = ""
    }
}