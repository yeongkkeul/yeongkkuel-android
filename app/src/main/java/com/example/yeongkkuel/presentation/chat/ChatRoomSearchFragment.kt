package com.example.yeongkkuel.presentation.chat

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
import com.example.yeongkkuel.databinding.FragmentChatRoomSearchBinding
import com.example.yeongkkuel.presentation.base.MainActivity
import com.example.yeongkkuel.presentation.chat.adapter.ChatRoomSearchAdapter

class ChatRoomSearchFragment : Fragment(), ChatRoomSearchClickListener {
    private lateinit var navController: NavController
    private var _binding: FragmentChatRoomSearchBinding? = null
    private val binding: FragmentChatRoomSearchBinding
        get() = requireNotNull(_binding){"FragmentChatRoomSearchBinding -> null"}

    private lateinit var chatRoomSearchAdapter: ChatRoomSearchAdapter

    private val viewModel: ChatSearchViewModel by activityViewModels()

    private lateinit var fullChatRoomList: List<ChatRoomSearch>

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

        binding.tvSearchTagAge.setOnClickListener {
            val bottomSheet = FilterAgeDialogFragment()
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }

        binding.tvSearchTagGoalExpense.setOnClickListener {
            val bottomSheet = FilterExpenseDialogFragment()
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }

        binding.tvSearchTagJob.setOnClickListener {
            val bottomSheet = FilterJobDialogFragment()
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }

        viewModel.selectedAgeOption.observe(viewLifecycleOwner) { option ->
            binding.tvSearchTagAge.text = option
            filterChatRooms()
        }

        viewModel.selectedExpenseOption.observe(viewLifecycleOwner) { option ->
            binding.tvSearchTagGoalExpense.text = option
        }

        viewModel.selectedJobOption.observe(viewLifecycleOwner) { option ->
            binding.tvSearchTagJob.text = option
            filterChatRooms()
        }
    }

    private fun filterChatRoomsByAge(selectedAge: String) {
        // 예시로 "전체" 혹은 빈 문자열일 경우 전체 리스트를 사용하게 처리
        val filteredList = if (selectedAge.isEmpty() || selectedAge == "전체") {
            fullChatRoomList
        } else {
            fullChatRoomList.filter { it.chatRoomAgeRange == selectedAge }
        }
        chatRoomSearchAdapter.updateList(filteredList)
    }

    private fun filterChatRooms() {
        // 현재 선택된 필터 옵션을 가져옵니다.
        val ageFilter = viewModel.selectedAgeOption.value ?: "전체"
        val jobFilter = viewModel.selectedJobOption.value ?: "전체"

        // 필터 값이 "전체"인 경우에는 해당 조건을 무시하도록 처리합니다.
        val filteredList = fullChatRoomList.filter { chatRoom ->
            val matchAge = (ageFilter == "전체") || (chatRoom.chatRoomAgeRange == ageFilter)
            val matchJob = (jobFilter == "전체") || (chatRoom.chatRoomJob == jobFilter)
            matchAge && matchJob
        }
        chatRoomSearchAdapter.updateList(filteredList)
    }


    private fun setupRecyclerView() {

        fullChatRoomList = generateDummyData(10)
        chatRoomSearchAdapter = ChatRoomSearchAdapter(fullChatRoomList, this)

        binding.rvChatRoom.apply {

            layoutManager = LinearLayoutManager(context)
            adapter = chatRoomSearchAdapter
        }
    }

    private fun generateDummyData(count: Int): List<ChatRoomSearch> {
        val ageRanges = listOf("20대", "30대", "40대", "50대")
        val jobs = listOf("학생", "직장인", "주부", "자영업자")
        return List(count) {
            val maxUserCount = (5..20).random()
            val dDay = (1..30).random()
            ChatRoomSearch(
                chatRoomId = (it + 1).toString(),
                chatRoomName = "채팅방 ${it + 1}",
                chatRoomAgeRange = ageRanges.random(),
                chatRoomMaxUserCount = maxUserCount.toString(),
                chatRoomThumbnail = "https://helios-i.mashable.com/imagery/articles/04GeUVUQwZxpTYXdqbocKH2/hero-image.fill.size_1248x702.v1722586579.jpg",
                chatRoomJob = jobs.random(),
                chatRoomDDay = dDay,
                chatRoomSpendingAmount = 10000
            )
        }
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