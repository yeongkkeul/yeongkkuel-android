package com.example.yeongkkuel.presentation.chat.room

import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.activity.addCallback
import androidx.core.view.GravityCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentChatGroupBinding
import com.example.yeongkkuel.presentation.auth.TokenManager
import com.example.yeongkkuel.presentation.base.MainActivity
import com.example.yeongkkuel.presentation.chat.ChatMessageClickListener
import com.example.yeongkkuel.presentation.chat.ChatRoomViewModel
import com.example.yeongkkuel.presentation.chat.adapter.ChatGroupAdapter
import com.example.yeongkkuel.presentation.chat.adapter.ChatRoomDrawerAdapter
import com.example.yeongkkuel.presentation.chat.dialog.ChatRoomGroupExitDialog
import com.example.yeongkkuel.presentation.chat.search.ChatSearchViewModel
import com.example.yeongkkuel.utils.ChatItemDecoration
import timber.log.Timber
import java.text.NumberFormat

class ChatGroupFragment : Fragment(), ChatMessageClickListener {
    private lateinit var navController: NavController
    private lateinit var chatGroupAdapter: ChatGroupAdapter

    private var _binding: FragmentChatGroupBinding? = null
    private val binding: FragmentChatGroupBinding
        get() = requireNotNull(_binding){"FragmentChatGroupBinding -> null"}

    private val viewModel: ChatSearchViewModel by activityViewModels()
    private val chatGroupViewModel: ChatGroupViewModel by activityViewModels {
        ChatRoomViewModelFactory(
            ChatRepository(
                ChatDatabase.getInstance(requireContext()).chatMessageCountDao()
            )
        )
    }
    private val chatRoomViewModel: ChatRoomViewModel by activityViewModels {
        ChatRoomViewModelFactory(
            ChatRepository(
                ChatDatabase.getInstance(requireContext()).chatMessageCountDao()
            )
        )
    }
    private var bannerOpen = false

    private lateinit var chatRoomDrawerAdapter: ChatRoomDrawerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        _binding = FragmentChatGroupBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onStop() {
        super.onStop()
        // 화면을 벗어나면 polling 중단
        chatGroupViewModel.stopPollingChatRooms()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).hideBottomNavigation(true)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            chatGroupViewModel.clearChatMessages()
            findNavController().navigateUp()
        }

        navController = Navigation.findNavController(view)

        val senderId = TokenManager.getUserId(requireContext())

        chatGroupViewModel.selectedChatRoomId.value?.let { chatRoomId ->
            chatGroupViewModel.fetchLatestTextMessageForChatRoom(chatRoomId, senderId)
            chatGroupViewModel.setupStompClient()
            chatGroupViewModel.enterChatRoomRegistered()
            viewModel.fetchChatDetail(chatRoomId) { detail ->
                detail ?.let {
                    binding.tvDataGoalSuccessChallenger.text = detail.chatRoomChallenger
                    binding.tvTitleChatGroup.text = detail.chatRoomTitle
                    binding.tvAmountPeopleChatGroup.text = detail.chatRoomChallenger.substringBefore("/")
                }
            }
        }

        binding.btnBack.setOnClickListener {
            chatGroupViewModel.clearChatMessages()
            navController.navigateUp()
        }

        binding.btnRank.setOnClickListener {
            navController.navigate(R.id.action_navigation_chat_room_group_to_rank)
        }

        binding.btnMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.END)
        }

        chatGroupViewModel.selectedChatRoomId.observe(viewLifecycleOwner) { chatRoomId ->
            if (chatRoomId != null) {
                chatGroupViewModel.fetchBanner(chatRoomId)

                chatRoomViewModel.chatRooms.observe(viewLifecycleOwner) { chatRooms ->
                    val targetChatRoom = chatRooms.find { it.id == chatRoomId }
                    val chatRoomRule = targetChatRoom?.chatRoomRule

                    chatRoomRule?.let {
                        binding.tvDataGroupRule.text = it
                        println("Chat Room Rule: $it")
                    } ?: run {
                        println("Chat Room with id 1 not found or rule is null")
                    }
                }
            }
        }

        chatGroupViewModel.bannerData.observe(viewLifecycleOwner) { banner ->
            banner?.let {
                binding.tvCreatedAt.text = it.createdAt
                // 금액을 천 단위로 포맷팅 후 "원" 단위 추가
                binding.tvDataExpenseAverage.text = "${NumberFormat.getInstance().format(it.avgAmount)} 원"
                // 연령과 직업을 결합하여 표시 (예: "20대 학생")
                binding.tvRankGroupAge.text = "${it.age} ${it.job}"
                // 상위 퍼센트 표시
                binding.tvDataRankGroupAge.text = "상위 ${it.topRate}%"
            }
        }

//        setupKeyboardListener()

        val otherProfileImageUrl = "https://helios-i.mashable.com/imagery/articles/04GeUVUQwZxpTYXdqbocKH2/hero-image.fill.size_1248x702.v1722586579.jpg"

        chatGroupAdapter = ChatGroupAdapter(emptyList(), otherProfileImageUrl, this)
        binding.rvChatGroup.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatGroupAdapter
        }

        val itemSpace = resources.getDimensionPixelSize(R.dimen.space_x_small)
        binding.rvChatGroup.addItemDecoration(ChatItemDecoration(itemSpace))

        chatGroupViewModel.messages.observe(viewLifecycleOwner) { messages ->
            Timber.tag("ChatFragment").d("글자 업데이트 완료: %s", messages)
            chatGroupAdapter = ChatGroupAdapter(messages.reversed(), otherProfileImageUrl, this)
            binding.rvChatGroup.adapter = chatGroupAdapter
            binding.rvChatGroup.scrollToPosition(messages.size - 1)
        }

        binding.btnSend.setOnClickListener {
            Timber.tag("chatGroupViewModel").d(chatGroupViewModel.message.value.toString())

            chatGroupViewModel.selectedChatRoomId.value?.let { chatRoomId ->
                chatGroupViewModel.sendMessageToChatRoom(chatRoomId.toLong(), senderId.toLong(), chatGroupViewModel.message.value.toString())
                chatGroupViewModel.enterChatRoomRegistered()
                chatGroupViewModel.fetchLatestTextMessageForChatRoom(chatRoomId, senderId)
            }

            binding.chatMessageInput.setText("")
        }

        binding.chatMessageInput.doAfterTextChanged {
            chatGroupViewModel.message.value = it.toString()
            Timber.tag("ChatFragment").d("글자 변화 감지: %s", it.toString())
        }

        binding.clChatGroupBanner.setOnClickListener {
            if (bannerOpen) {
                // 버튼 숨김
                animateButtonVisibility(false)
                binding.ivBannerArrow.setImageResource(R.drawable.ic_arrow_bottom)
                bannerOpen = false
            } else {
                // 버튼 펼침
                animateButtonVisibility(true)
                binding.ivBannerArrow.setImageResource(R.drawable.ic_arrow_top)
                bannerOpen = true
            }
        }

        // 배너 삭제
        binding.btnDeleteBanner.setOnClickListener {
            binding.clChatGroupBanner.visibility = View.GONE
        }

        // 접어두기 버튼 클릭 처리
        binding.btnFoldBanner.setOnClickListener {
            animateButtonVisibility(false)
            binding.ivBannerArrow.setImageResource(R.drawable.ic_arrow_bottom)
            bannerOpen = false
        }

        chatRoomDrawerAdapter = ChatRoomDrawerAdapter(arrayListOf())

        binding.rvGroupChallenger.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatRoomDrawerAdapter
        }

        binding.apply {
            Glide.with(ivPhoto1)
                .load(otherProfileImageUrl)
                .into(ivPhoto1)
            Glide.with(ivPhoto2)
                .load(otherProfileImageUrl)
                .into(ivPhoto2)
            Glide.with(ivPhoto3)
                .load(otherProfileImageUrl)
                .into(ivPhoto3)
        }

        binding.btnExit.setOnClickListener {
            val dialog = ChatRoomGroupExitDialog(
                context = requireContext(),
                onCancelClick = {  },
                onExitClick = {
                    navController.navigateUp()
                }
            )
            dialog.show()
        }
    }

    private fun animateButtonVisibility(show: Boolean) {
        val transition = AutoTransition().apply {
            duration = 300
            interpolator = DecelerateInterpolator()
        }

        TransitionManager.beginDelayedTransition(binding.clChatGroupBanner, transition)

        binding.btnDeleteBanner.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnFoldBanner.visibility = if (show) View.VISIBLE else View.GONE
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

    override fun onMessageClicked() {
    }
}