package com.example.yeongkkuel.presentation.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentChatRoomSearchBinding
import com.example.yeongkkuel.presentation.base.MainActivity
import com.example.yeongkkuel.presentation.chat.adapter.ChatRoomSearchAdapter

class ChatRoomSearchFragment : Fragment(), ChatRoomSearchClickListener {
    private lateinit var navController: NavController
    private var _binding: FragmentChatRoomSearchBinding? = null
    private val binding: FragmentChatRoomSearchBinding
        get() = requireNotNull(_binding){"FragmentChatRoomSearchBinding -> null"}

    private lateinit var chatRoomSearchAdapter: ChatRoomSearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentChatRoomSearchBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        (requireActivity() as MainActivity).hideBottomNavigation(false)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {

        val dummyData = generateDummyData(10)
        chatRoomSearchAdapter = ChatRoomSearchAdapter(dummyData, this)

        binding.rvChatRoom.apply {

            layoutManager = LinearLayoutManager(context)
            adapter = chatRoomSearchAdapter
        }
    }

    private fun generateDummyData(count: Int): ArrayList<ChatRoomSearch> {
        return ArrayList(List(count) { index ->
            ChatRoomSearch(
                chatRoomId = (index + 1).toString(),
                chatRoomName = "채팅방 ${index + 1}",
                chatRoomAgeRange = "20대",
                chatRoomMaxUserCount = "10",
                chatRoomThumbnail = "https://helios-i.mashable.com/imagery/articles/04GeUVUQwZxpTYXdqbocKH2/hero-image.fill.size_1248x702.v1722586579.jpg",
                chatRoomJob = "개발자",
                chatRoomDDay = 10,
                chatRoomSpendingAmount = 10000
            )
        })
    }

    override fun onItemClicked(chatRoom: ChatRoomSearch) {
        // 아이템 클릭 시 실행할 로직
        showToast("Clicked: ${chatRoom.chatRoomName}")
        navController.navigate(R.id.action_navigation_chat_room_search_to_register)
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