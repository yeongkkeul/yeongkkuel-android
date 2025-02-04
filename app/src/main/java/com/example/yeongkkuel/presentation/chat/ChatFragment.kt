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
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentChatBinding
import com.example.yeongkkuel.presentation.base.MainActivity
import com.example.yeongkkuel.presentation.chat.adapter.ChatRoomAdapter
import com.example.yeongkkuel.presentation.chat.dialog.FabMenuDialog
import com.example.yeongkkuel.utils.SwipeToDelete
import com.google.android.material.floatingactionbutton.FloatingActionButton
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

        (requireActivity() as MainActivity).hideBottomNavigation(false)

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
    }

    private fun openDialog(hideFab: FloatingActionButton, showFab: FloatingActionButton) {
        Timber.d("openDialog")

        val dialog = FabMenuDialog(
            context = requireContext(),
            anchorView = hideFab,
            onDismissCallback = { closeDialog(hideFab, showFab) },
            onMenuItem1Click = {  },
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

        val dummyData = generateDummyData(4)
        chatRoomAdapter = ChatRoomAdapter(dummyData, this, binding.rvChatRoom, swipeToDelete)

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

    private fun generateDummyData(count: Int): ArrayList<ChatRoom> {
        return ArrayList(List(count) { index ->
            ChatRoom(
                id = (index + 1).toString(),
                title = "채팅방 ${index + 1}",
                thumbnailUrl = "https://helios-i.mashable.com/imagery/articles/04GeUVUQwZxpTYXdqbocKH2/hero-image.fill.size_1248x702.v1722586579.jpg",
                recentMessage = "최근 메시지 ${index + 1}",
                messageTime = "오후 2:${30 + index % 10}",
                participantCount = 10 + index
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