package com.example.yeongkkuel.presentation.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.databinding.FragmentChatBinding
import com.example.yeongkkuel.utils.SwipeToDelete
import timber.log.Timber

class ChatFragment : Fragment(), ChatRoomClickListener {
    private lateinit var navController: NavController
    private var _binding: FragmentChatBinding? = null
    private val binding: FragmentChatBinding
        get() = requireNotNull(_binding){"FragmentChatBinding -> null"}

    private lateinit var chatRoomAdapter: ChatRoomAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentChatBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        setupRecyclerView()
        loadDummyData()
    }

    private fun setupRecyclerView() {
        chatRoomAdapter = ChatRoomAdapter(arrayListOf(), this)

        val swipeToDelete = SwipeToDelete().apply {
            setClamp(resources.displayMetrics.widthPixels.toFloat() / 5)
        }
        ItemTouchHelper(swipeToDelete).attachToRecyclerView(binding.rvChatRoom)

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

    private fun loadDummyData() {
        val dummyData = generateDummyData(15)
        chatRoomAdapter = ChatRoomAdapter(dummyData, this)
        binding.rvChatRoom.adapter = chatRoomAdapter
        toggleEmptyView()
    }

    private fun generateDummyData(count: Int): ArrayList<ChatRoom> {
        return ArrayList(List(count) { index ->
            ChatRoom(
                id = (index + 1).toString(), // id를 1부터 시작하도록 설정
                title = "채팅방 ${index + 1}",
                thumbnailUrl = "https://helios-i.mashable.com/imagery/articles/04GeUVUQwZxpTYXdqbocKH2/hero-image.fill.size_1248x702.v1722586579.jpg",
                recentMessage = "최근 메시지 ${index + 1}",
                messageTime = "오후 2:${30 + index % 10}", // 예제 시간 가변 적용
                participantCount = 10 + index // 참가자 수 증가
            )
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