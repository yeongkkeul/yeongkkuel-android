package com.example.yeongkkuel.presentation.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentChatRoomRegisterBinding
import com.example.yeongkkuel.network.response.chat.ChatDetailResult
import com.example.yeongkkuel.presentation.chat.data.Age
import com.example.yeongkkuel.presentation.chat.data.Job
import com.example.yeongkkuel.presentation.chat.dialog.ChatRoomExpelDialog
import com.example.yeongkkuel.presentation.chat.dialog.ChatRoomExpenseAutoSendDialog
import com.example.yeongkkuel.presentation.chat.dialog.ChatRoomPwDialog
import com.example.yeongkkuel.presentation.chat.search.ChatSearchViewModel
import java.text.NumberFormat
import java.util.Locale

class ChatRoomRegisterFragment : Fragment() {

    private lateinit var navController: NavController
    private var _binding: FragmentChatRoomRegisterBinding? = null
    private val binding: FragmentChatRoomRegisterBinding
        get() = requireNotNull(_binding){"FragmentChatRoomRegisterBinding -> null"}

    private val expel = false

    private val viewModel: ChatSearchViewModel by activityViewModels()

    private var chatDetail: ChatDetailResult? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentChatRoomRegisterBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        viewModel.selectedChatRoomId.value?.let { chatRoomId ->
            viewModel.fetchChatDetail(chatRoomId) { detail ->
                detail ?.let {
                    chatDetail = it  // 상세 정보 저장
                    displayChatDetail(it)
                }
            }
        }

        binding.btnBack.setOnClickListener {
            navController.navigateUp()
        }

        binding.btnRegister.setOnClickListener {
            // isPassword가 true이면 비밀번호 다이얼로그 띄움, 아니면 바로 다음 단계 진행
            if (chatDetail?.isPassword == true) {
                val dialog = ChatRoomPwDialog(
                    context = requireContext(),
                    chatRoomId = viewModel.selectedChatRoomId.value ?: 0,
                    onCancelClick = { /* 취소 처리 */ },
                    onConfirmClick = { expenseAutoSendDialogShow() }
                )
                dialog.show()
            } else {
                expenseAutoSendDialogShow()
            }
        }
    }

    private fun displayChatDetail(detail: ChatDetailResult) {
        // 채팅방 썸네일 로딩
        Glide.with(requireContext())
            .load(detail.chatRoomImageUrl)
            .into(binding.ivChatRoomThumbnail)

        // 채팅방 제목
        binding.tvTitleChatRoom.text = detail.chatRoomTitle

        // 마지막 활동 시간 (예: "30분 전 활동")
        binding.tvTimeLastMessage.text = detail.lastActivity

        binding.tvTagAge.text = try {
            Age.valueOf(detail.chatRoomAgeRange).displayName
        } catch (e: Exception) {
            detail.chatRoomAgeRange
        }

        binding.tvTagStatus.text = try {
            Job.valueOf(detail.chatRoomJob).displayName
        } catch (e: Exception) {
            detail.chatRoomJob
        }

        // 생성된 일수(혹은 경과일) 태그
        binding.tvTagDays.text = detail.createdDaysElapsed

        // 참여 현황: 예를 들어 "현재 참여수 / 최대 참여수"
        binding.tvDataChallenger.text = "${detail.participationCount}/${detail.chatRoomMaxUserCount}"

        // 하루 목표 지출액: 3자리마다 콤마 처리 후 "원" 추가
        val formattedExpense = NumberFormat.getNumberInstance(Locale.getDefault())
            .format(detail.chatRoomSpendingAmountGoal) + "원"
        binding.tvDataDailyExpense.text = formattedExpense
    }

    private fun expenseAutoSendDialogShow() {
        val dialog = ChatRoomExpenseAutoSendDialog(
            context = requireContext(),
            onCancelClick = {  },
            onConfirmClick = { checkExpel() }
        )
        dialog.show()
    }

    private fun checkExpel() {
        if (expel) {
            val dialog = ChatRoomExpelDialog(
                context = requireContext(),
                onConfirmClick = { }
            )
            dialog.show()
        } else {
            navController.navigate(R.id.action_navigation_chat_room_register_to_chat_group)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}