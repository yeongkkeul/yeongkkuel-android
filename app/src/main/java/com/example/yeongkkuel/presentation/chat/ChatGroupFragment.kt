package com.example.yeongkkuel.presentation.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.activity.addCallback
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentChatGroupBinding
import com.example.yeongkkuel.presentation.base.MainActivity
import com.example.yeongkkuel.presentation.chat.adapter.ChatGroupAdapter
import com.example.yeongkkuel.utils.ChatItemDecoration
import timber.log.Timber

class ChatGroupFragment : Fragment() {
    private lateinit var navController: NavController
    private lateinit var chatGroupAdapter: ChatGroupAdapter

    private var _binding: FragmentChatGroupBinding? = null
    private val binding: FragmentChatGroupBinding
        get() = requireNotNull(_binding){"FragmentChatGroupBinding -> null"}

    private val chatGroupViewModel: ChatGroupViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        _binding = FragmentChatGroupBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).hideBottomNavigation(true)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            chatGroupViewModel.clearChatMessages()
            findNavController().navigateUp()
        }

        navController = Navigation.findNavController(view)

        binding.btnBack.setOnClickListener {
            chatGroupViewModel.clearChatMessages()
            navController.navigateUp()
        }

        binding.btnRank.setOnClickListener {
            navController.navigate(R.id.action_navigation_chat_room_group_to_rank)
        }

//        setupKeyboardListener()

        val otherProfileImageUrl = "https://helios-i.mashable.com/imagery/articles/04GeUVUQwZxpTYXdqbocKH2/hero-image.fill.size_1248x702.v1722586579.jpg"

        chatGroupAdapter = ChatGroupAdapter(emptyList(), otherProfileImageUrl)
        binding.rvChatGroup.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatGroupAdapter
        }

        val itemSpace = resources.getDimensionPixelSize(R.dimen.space_x_small)
        binding.rvChatGroup.addItemDecoration(ChatItemDecoration(itemSpace))

        chatGroupViewModel.messages.observe(viewLifecycleOwner) { messages ->
            Timber.tag("ChatFragment").d("글자 업데이트 완료: %s", messages)
            chatGroupAdapter = ChatGroupAdapter(messages, otherProfileImageUrl)
            binding.rvChatGroup.adapter = chatGroupAdapter
            binding.rvChatGroup.scrollToPosition(messages.size - 1)
        }

        binding.btnSend.setOnClickListener {
            Timber.tag("chatGroupViewModel").d(chatGroupViewModel.message.value.toString())

            // GPT 답변을 얻기 위한 openAI 서버에 질문 전송
            chatGroupViewModel.sendMessage()

            binding.chatMessageInput.setText("")
        }

        binding.chatMessageInput.doAfterTextChanged {
            chatGroupViewModel.message.value = it.toString()
            Timber.tag("ChatFragment").d("글자 변화 감지: %s", it.toString())
        }

    }

//    private fun setupKeyboardListener() {
//        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
//            val heightDiff = binding.root.rootView.height - binding.root.height
//            if (heightDiff > 300) { // 키보드가 올라온 경우
//                binding.rvChatGroup.scrollToPosition(chatGroupAdapter.itemCount - 1)
//            }
//        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}