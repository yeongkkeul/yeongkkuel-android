package com.example.yeongkkuel.presentation.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentChatBinding
import com.example.yeongkkuel.presentation.base.MainActivity
import com.example.yeongkkuel.presentation.chat.adapter.ChatRoomAdapter
import com.example.yeongkkuel.presentation.chat.dialog.FabMenuDialog
import com.example.yeongkkuel.presentation.chat.room.ChatGroupViewModel
import com.example.yeongkkuel.presentation.chat.room.ChatRoomClickListener
import com.example.yeongkkuel.utils.SwipeToDelete
import com.google.android.material.floatingactionbutton.FloatingActionButton
import timber.log.Timber

class ChatFragment : Fragment(), ChatRoomClickListener {
    private lateinit var navController: NavController
    private var _binding: FragmentChatBinding? = null
    private val binding: FragmentChatBinding
        get() = requireNotNull(_binding){"FragmentChatBinding -> null"}

    private lateinit var chatRoomAdapter: ChatRoomAdapter

//    private lateinit var stompClient: StompClient
//
//    private val compositeDisposable = CompositeDisposable()

    private val viewModel: ChatRoomViewModel by activityViewModels()
    private val chatGroupViewModel: ChatGroupViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        _binding = FragmentChatBinding.inflate(inflater, container, false)

        return binding.root
    }

//    private fun createOkHttpClient(): OkHttpClient {
//        return OkHttpClient.Builder()
//            .readTimeout(0, TimeUnit.MILLISECONDS)
//            .build()
//    }

//    private fun setupStompClient() {
//        val okHttpClient = createOkHttpClient()
//        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "wss://dev.yeongkkeul.store/ws")
//
//        // STOMP 클라이언트의 생명주기 이벤트 구독 (RxJava 사용)
//        val lifecycleDisposable = stompClient.lifecycle()
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe { lifecycleEvent ->
//                when (lifecycleEvent.type) {
//                    LifecycleEvent.Type.OPENED -> {
//                        Timber.tag("STOMP").d("연결 성공: %s", lifecycleEvent)
//                        // 연결 성공 후 채팅방 가입 요청 실행 (예시: chatRoomId = 123, senderId = 456)
//                        joinChatRoom(chatRoomId = 2L, senderId = 8L, password = "1234")
//                        // 필요 시 채팅방 수신 메시지 구독 (예: subscribeToChatRoom(chatRoomId))
//                    }
//                    LifecycleEvent.Type.ERROR -> {
//                        Timber.tag("STOMP").e(lifecycleEvent.exception, "연결 에러: ")
//                    }
//                    LifecycleEvent.Type.CLOSED -> {
//                        Timber.tag("STOMP").d("연결 종료됨")
//                    }
//                    else -> {}
//                }
//            }
//        compositeDisposable.add(lifecycleDisposable)
//
//        // 웹소켓 연결 시작
//        stompClient.connect()
//    }

//    private fun joinChatRoom(chatRoomId: Long, senderId: Long, password: String?) {
//        // 채팅방 가입 URL 구성 (roomId 자리에 chatRoomId 값 삽입)
//        val destination = "/pub/chat.enter.$chatRoomId"
//
//        // JSON 메시지 구성 (content는 빈 문자열로 설정)
//        val jsonMessage = JSONObject().apply {
//            put("chatRoomId", chatRoomId)
//            put("senderId", senderId)
//            put(
//                "messageType",
//                "ENTER"
//            )
//            put("content", "twosome")
//            // password가 null인 경우 JSON_NULL로 명시
//            put("password", password ?: JSONObject.NULL)
//        }
//
//        Timber.d("$jsonMessage")
//
//        // 메시지 publish (RxJava Observable 구독)
//        val sendDisposable = stompClient.send(destination, jsonMessage.toString())
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe({
//                Log.d("STOMP", "채팅방 가입 메시지 전송 성공")
//            }, { error ->
//                Log.e("STOMP", "채팅방 가입 메시지 전송 실패: ${error.message}")
//            })
//
//        compositeDisposable.add(sendDisposable)
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        (requireActivity() as MainActivity).hideBottomNavigation(false)

//        setupStompClient()

        // 기존 FAB 클릭 리스너
        binding.floatingActionButton.setOnClickListener {
            openDialog(binding.floatingActionButton, binding.floatingActionButtonCancel)
            animateFab(binding.floatingActionButton, binding.floatingActionButtonCancel)
            Timber.d("floatingActionButton")
        }

        // Cancel FAB 클릭 리스너
        binding.floatingActionButtonCancel.setOnClickListener {
            closeDialog(binding.floatingActionButtonCancel, binding.floatingActionButton)
            Timber.d("floatingActionButtonCancel")
        }

        setupRecyclerView()

        viewModel.chatRooms.observe(viewLifecycleOwner) { chatRooms ->
            chatRoomAdapter.updateData(ArrayList(chatRooms))
        }
    }

    private fun openDialog(hideFab: FloatingActionButton, showFab: FloatingActionButton) {
        Timber.d("openDialog")

        val dialog = FabMenuDialog(
            context = requireContext(),
            anchorView = hideFab,
            onDismissCallback = { closeDialog(hideFab, showFab) },
            onMenuItem1Click = { navController.navigate(R.id.action_navigation_chat_to_navigation_chat_room_create) },
            onMenuItem2Click = { navController.navigate(R.id.action_navigation_chat_to_navigation_chat_room_search) }
        )
        dialog.show()

        binding.dimView.visibility = View.VISIBLE
    }

    private fun closeDialog(hideFab: FloatingActionButton, showFab: FloatingActionButton) {
        Timber.d("closeDialog")

        // 나타나는 버튼에 회전 애니메이션
        showFab.visibility = View.VISIBLE
        showFab.rotation = -180f // 초기 상태: -180도
        showFab.animate()
            .rotation(0f) // 원래 상태로 회전
            .setDuration(300)
            .start()

        // 숨기는 버튼에 회전 애니메이션
        hideFab.animate()
            .rotation(180f) // 180도 회전
            .setDuration(300)
            .withEndAction {
                hideFab.visibility = View.GONE // 애니메이션 종료 후 숨김
            }
            .start()

        binding.dimView.visibility = View.GONE
    }

    private fun animateFab(hideFab: FloatingActionButton, showFab: FloatingActionButton) {
        // 숨기는 버튼에 회전 애니메이션
        hideFab.animate()
            .rotation(180f) // 180도 회전
            .setDuration(300)
            .withEndAction {
                hideFab.visibility = View.GONE // 애니메이션 종료 후 숨김
            }
            .start()

        // 나타나는 버튼에 회전 애니메이션
        showFab.visibility = View.VISIBLE
        showFab.rotation = -180f // 초기 상태: -180도
        showFab.animate()
            .rotation(0f) // 원래 상태로 회전
            .setDuration(300)
            .start()
    }

    private fun setupRecyclerView() {
        val swipeToDelete = SwipeToDelete().apply {
            setClamp(resources.displayMetrics.widthPixels.toFloat() / 5)
        }
        ItemTouchHelper(swipeToDelete).attachToRecyclerView(binding.rvChatRoom)

        chatRoomAdapter = ChatRoomAdapter(ArrayList(), this, binding.rvChatRoom, swipeToDelete)

        binding.rvChatRoom.apply {

            layoutManager = LinearLayoutManager(context)
            adapter = chatRoomAdapter

            setOnTouchListener { v, _ ->
                swipeToDelete.removePreviousClamp(this)
                v.performClick()
                invalidateItemDecorations()
                false
            }

            setOnClickListener {
            }
        }

        chatRoomAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                toggleEmptyView()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                toggleEmptyView()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                toggleEmptyView()
            }
        })
    }

    override fun onItemDeleted(chatRoom: ChatRoom) {
        // 아이템 삭제 로직
        val updatedList = ArrayList(chatRoomAdapter.chatRooms.filter { it.id != chatRoom.id })
        chatRoomAdapter.updateData(updatedList)
        toggleEmptyView()
    }


    override fun onItemClicked(chatRoom: ChatRoom) {
        // 아이템 클릭 시 실행할 로직
        showToast("Clicked: ${chatRoom.title}")
        chatGroupViewModel.setSelectedChatRoomId(chatRoom.id)
        navController.navigate(R.id.action_navigation_chat_to_navigation_chat_group)
    }

    private fun toggleEmptyView() {
        Timber.d("toggleEmptyView")
        if (chatRoomAdapter.itemCount == 0) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.rvChatRoom.visibility = View.INVISIBLE
        } else {
            binding.layoutEmpty.visibility = View.INVISIBLE
            binding.rvChatRoom.visibility = View.VISIBLE
        }
    }

    private fun showToast(message: String) {
        context?.let {
            android.widget.Toast.makeText(it, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}