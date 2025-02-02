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
import com.example.yeongkkuel.databinding.FragmentChatRoomRankBinding
import com.example.yeongkkuel.presentation.base.MainActivity
import com.example.yeongkkuel.presentation.chat.adapter.ChatRoomRankAdapter
import com.example.yeongkkuel.presentation.chat.data.ChatRoomRank
import com.example.yeongkkuel.presentation.chat.dialog.ChatRoomRankPopup
import com.example.yeongkkuel.utils.ChatItemDecoration

class ChatRoomRankFragment : Fragment() {

    private lateinit var navController: NavController
    private var _binding: FragmentChatRoomRankBinding? = null
    private val binding: FragmentChatRoomRankBinding
        get() = requireNotNull(_binding){"FragmentChatRoomRankBinding -> null"}

    private lateinit var chatRoomRankAdapter: ChatRoomRankAdapter

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
        loadDummyData()
    }

    private fun setupRecyclerView() {
        chatRoomRankAdapter = ChatRoomRankAdapter(arrayListOf())

        binding.rvChatRoomRank.apply {

            layoutManager = LinearLayoutManager(context)
            adapter = chatRoomRankAdapter

            val itemSpace = resources.getDimensionPixelSize(R.dimen.space_chat_room_rank)
            addItemDecoration(ChatItemDecoration(itemSpace))
        }
    }

    private fun loadDummyData() {
        val dummyData = generateDummyData(20)
        chatRoomRankAdapter = ChatRoomRankAdapter(dummyData)
        binding.rvChatRoomRank.adapter = chatRoomRankAdapter
    }

    private fun generateDummyData(count: Int): ArrayList<ChatRoomRank> {
        return ArrayList(List(count) { index ->
            ChatRoomRank(
                nickname = "사용자 ${index + 1}",
                profileImage = "https://helios-i.mashable.com/imagery/articles/04GeUVUQwZxpTYXdqbocKH2/hero-image.fill.size_1248x702.v1722586579.jpg",
                rankScore = 100 - index,
                rank = index +1
            )
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}