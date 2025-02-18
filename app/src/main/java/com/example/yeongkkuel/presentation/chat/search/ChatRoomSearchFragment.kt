package com.example.yeongkkuel.presentation.chat.search

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentChatRoomSearchBinding
import com.example.yeongkkuel.network.response.chat.ChatRoomDetailDto
import com.example.yeongkkuel.presentation.base.MainActivity
import com.example.yeongkkuel.presentation.chat.room.ChatGroupViewModel
import com.example.yeongkkuel.presentation.chat.adapter.ChatRoomSearchAdapter
import timber.log.Timber

class ChatRoomSearchFragment : Fragment(), ChatRoomSearchClickListener {
    private lateinit var navController: NavController
    private var _binding: FragmentChatRoomSearchBinding? = null
    private val binding: FragmentChatRoomSearchBinding
        get() = requireNotNull(_binding){"FragmentChatRoomSearchBinding -> null"}

    private lateinit var chatRoomSearchAdapter: ChatRoomSearchAdapter

    private val viewModel: ChatSearchViewModel by activityViewModels()
    private val chatGroupViewModel: ChatGroupViewModel by activityViewModels()

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

        (requireActivity() as MainActivity).hideBottomNavigation(true)

        viewModel.fetchChatRooms()

        setupRecyclerView()

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val keyword = s.toString().trim()
                if (keyword.length >= 2) {
                    viewModel.searchChatRooms(keyword)
                } else {
                    viewModel.fetchChatRooms()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnSearch.setOnClickListener {
            hideKeyboard()
            val keyword = binding.etSearch.text.toString().trim()
            if (keyword.length >= 2) {
                viewModel.searchChatRooms(keyword)
            } else {
                showToast("검색어는 2글자 이상 입력해주세요.")
                viewModel.fetchChatRooms()
            }
        }

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

        viewModel.selectedAgeOption.observe(viewLifecycleOwner) { selectedAge ->
            binding.tvSearchTagAge.text = selectedAge?: "전체"
            viewModel.chatRoomList.value?.let { list ->
                applyFiltersAndUpdateAdapter(list)
            }
        }

        viewModel.selectedExpenseOption.observe(viewLifecycleOwner) { option ->
            binding.tvSearchTagGoalExpense.text = option
        }

        viewModel.selectedJobOption.observe(viewLifecycleOwner) { selectedJob ->
            binding.tvSearchTagJob.text = selectedJob ?: "전체"
            viewModel.chatRoomList.value?.let { list ->
                applyFiltersAndUpdateAdapter(list)
            }
        }

        viewModel.chatRoomList.observe(viewLifecycleOwner) { list ->
            applyFiltersAndUpdateAdapter(list)
        }

        binding.root.apply {
            setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        hideKeyboard()
                        v.performClick()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun applyFiltersAndUpdateAdapter(list: MutableList<ChatRoomDetailDto>) {
        val selectedAge = viewModel.selectedAgeOption.value
        val selectedJob = viewModel.selectedJobOption.value
        Timber.d("selectedAge: $selectedAge, selectedJob: $selectedJob")

        val filteredList = list.filter { chatRoom ->
            val matchesAge = selectedAge?.let { chatRoom.chatRoomAgeRange == it } ?: true
            val matchesJob = selectedJob?.let { chatRoom.chatRoomJob == it } ?: true
            matchesAge && matchesJob
        }
        Timber.d("filteredList: $filteredList")
        chatRoomSearchAdapter.updateList(filteredList.toMutableList())
    }

    private fun setupRecyclerView() {
        chatRoomSearchAdapter = ChatRoomSearchAdapter(mutableListOf(), this)
        binding.rvChatRoom.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatRoomSearchAdapter
        }
    }

    override fun onItemClicked(chatRoomSearch: ChatRoomDetailDto) {
        // 아이템 클릭 시 실행할 로직
        showToast("Clicked: ${chatRoomSearch.chatRoomTitle}")
        chatGroupViewModel.setSelectedChatRoomId(chatRoomSearch.chatRoomId)
        navController.navigate(R.id.action_navigation_chat_room_search_to_register)
    }

    private fun showToast(message: String) {
        context?.let {
            android.widget.Toast.makeText(it, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusView = activity?.currentFocus ?: binding.root
        imm.hideSoftInputFromWindow(currentFocusView.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}