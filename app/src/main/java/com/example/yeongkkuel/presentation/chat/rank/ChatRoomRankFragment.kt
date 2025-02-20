package com.example.yeongkkuel.presentation.chat.rank

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentChatRoomRankBinding
import com.example.yeongkkuel.presentation.base.MainActivity
import com.example.yeongkkuel.presentation.chat.adapter.ChatRoomRankAdapter
import com.example.yeongkkuel.network.response.chat.ChatRoomRank
import com.example.yeongkkuel.presentation.chat.room.ChatGroupViewModel
import com.example.yeongkkuel.presentation.chat.dialog.ChatRoomProfilePartyDialog
import com.example.yeongkkuel.presentation.chat.dialog.ChatRoomRankPopup
import com.example.yeongkkuel.presentation.chat.room.ChatDatabase
import com.example.yeongkkuel.presentation.chat.room.ChatRepository
import com.example.yeongkkuel.presentation.chat.room.ChatRoomViewModelFactory
import com.example.yeongkkuel.utils.ChatItemDecoration

class ChatRoomRankFragment : Fragment(), ChatRoomRankClickListener {

    private lateinit var navController: NavController
    private var _binding: FragmentChatRoomRankBinding? = null
    private val binding: FragmentChatRoomRankBinding
        get() = requireNotNull(_binding){"FragmentChatRoomRankBinding -> null"}

    private lateinit var chatRoomRankAdapter: ChatRoomRankAdapter

    private val chatGroupViewModel: ChatGroupViewModel by activityViewModels {
        ChatRoomViewModelFactory(
            ChatRepository(
                ChatDatabase.getInstance(requireContext()).chatMessageCountDao()
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentChatRoomRankBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        (requireActivity() as MainActivity).hideBottomNavigation(true)

        val chatRoomId = chatGroupViewModel.selectedChatRoomId.value
        if (chatRoomId != null) {
            chatGroupViewModel.getChatRoomRanks(chatRoomId)
        }

        binding.btnInfoRank.setOnClickListener {
            ChatRoomRankPopup(
                context = requireContext(),
                anchorView = binding.tvTitleChatRank,
            )
        }

        binding.btnBack.setOnClickListener {
            navController.navigateUp()
        }

        setupRecyclerView()

        chatGroupViewModel.chatRoomRanks.observe(viewLifecycleOwner) { ranks ->
            chatRoomRankAdapter.updateData(ranks)
        }
    }

    private fun setupRecyclerView() {
        chatRoomRankAdapter = ChatRoomRankAdapter(emptyList(), this)
        binding.rvChatRoomRank.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatRoomRankAdapter

            val itemSpace = resources.getDimensionPixelSize(R.dimen.space_chat_room_rank)
            addItemDecoration(ChatItemDecoration(itemSpace))
        }
    }

    override fun onRankItemClick(item: ChatRoomRank) {
        // chatRoomId는 선택된 채팅방 ID를 사용
        val chatRoomId = chatGroupViewModel.selectedChatRoomId.value ?: return

        // 클릭한 아이템의 userId로 API 호출
        chatGroupViewModel.fetchChatroomUser(chatRoomId, item.userId) { userResult ->
            if (userResult != null) {
                ChatRoomProfilePartyDialog(requireContext(), userResult) {
                    // 취소 클릭 시 처리할 내용 (필요하다면)
                }.show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}