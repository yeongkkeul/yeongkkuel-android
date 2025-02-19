package com.example.yeongkkuel.presentation.my

import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentNotificationBinding
import com.example.yeongkkuel.network.RetrofitClient
import com.example.yeongkkuel.network.RetrofitClient.notificationService
import com.example.yeongkkuel.network.request.notification.NotificationSettingRequest
import com.example.yeongkkuel.network.response.notification.NotificationDetail
import com.example.yeongkkuel.presentation.my.notification.NotificationAdapter
import com.example.yeongkkuel.presentation.my.notification.data.NotificationItem
import com.example.yeongkkuel.presentation.my.notification.data.NotificationType
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class NotificationFragment : Fragment() {

    private lateinit var binding: FragmentNotificationBinding  // viewBinding 사용하는 경우

    private lateinit var adapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onselectedListener()
//        checkAllNotificationRead()
        adapter = NotificationAdapter(
            items = emptyList(),
            onItemClick = { clickedItem ->
                navigateToFragment(clickedItem.type)
            }
        )

        binding.rvNotification.adapter = adapter
        binding.rvNotification.layoutManager = LinearLayoutManager(requireContext())
        fetchNotificationsFromServer()

    }

    private fun fetchNotificationsFromServer() {
        // 서버로부터 알림 목록을 가져오는 API 호출
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = notificationService.getNotificationList(page = 1)
                if (response.isSuccess) {
                    val body = response.result
                    if (body != null) {
                        // 3) notificationDetails -> NotificationItem으로 변환
                        val detailList = body.notificationDetails
                        val dummyData = listOf(
                            NotificationItem(
                                NotificationType.CHALLENGE_JOIN,
                                "무지출이 대세다 방 가입 완료",
                                "37분 전",
                                "오늘"
                            ),
                            NotificationItem(
                                NotificationType.NO_SPEND_REWARD,
                                "돈 모아서 차 사자 방 가입 완료",
                                "37분 전",
                                "오늘"
                            ),
                            NotificationItem(
                                NotificationType.RANKING_REWARD,
                                "랭킹 리워드 지급",
                                "37분 전",
                                "오늘"
                            ),
                            NotificationItem(
                                NotificationType.DAILY_EXCEED,
                                "하루 지출 목표액 초과",
                                "12:49",
                                "어제"
                            ),
                            NotificationItem(
                                NotificationType.NO_SPEND_REWARD,
                                "무지출 리워드 지급",
                                "12:49",
                                "어제"
                            ),
                            NotificationItem(
                                NotificationType.CHALLENGE_RANKING_UPDATE,
                                "12월 31일 챌린지 그룹 랭킹 업데이트",
                                "12:00",
                                "어제"
                            ),
                            NotificationItem(
                                NotificationType.CHALLENGE_JOIN,
                                "무지출이 대세다 방 가입 완료",
                                "12/12",
                                "최근 7일"
                            )
                        )

                        val itemList = detailList.map { mapToNotificationItem(it) }
                        if(itemList.isEmpty()){
                            adapter = NotificationAdapter(dummyData) { clickedItem ->
                                navigateToFragment(clickedItem.type)
                            }
                            binding.rvNotification.adapter = adapter
                        } else {
                            adapter = NotificationAdapter(itemList) { clickedItem ->
                                navigateToFragment(clickedItem.type)
                            }
                            binding.rvNotification.adapter = adapter
                        }
                    }
                } else {
                    // 실패 처리: response.errorBody()?.string() 등
                    Log.d("NotificationFragment", "알림 목록 가져오기 실패")
                }
            } catch (e: Exception) {
                // 네트워크 실패
                e.printStackTrace()
            }
        }
    }

    private fun mapToNotificationItem(detail: NotificationDetail): NotificationItem {
        // 여기에서 server의 detail -> UI용 item
        val typeEnum = stringToNotificationType(detail.notificationType)
        val message = detail.notificationContent
        val timeText = detail.timestamp
        val section = makeSectionLabel(detail.createdAt)

        return NotificationItem(typeEnum, message, timeText, section)
    }

    private fun stringToNotificationType(typeStr: String): NotificationType {
        return when (typeStr) {
            "JOIN_CHALLENGER_ROOM" -> NotificationType.CHALLENGE_JOIN
            "AWARD_RANKING_REWARDS" -> NotificationType.RANKING_REWARD
            "AWARD_NO_SPENDING_REWARDS" -> NotificationType.NO_SPEND_REWARD
            "EXCEED_DAILY_SPENDING_GOAL" -> NotificationType.DAILY_EXCEED
            "UPDATE_CHALLENGE_GROUP_RANKING" -> NotificationType.CHALLENGE_RANKING_UPDATE
            else -> NotificationType.CHALLENGE_JOIN
        }
    }


    private fun makeSectionLabel(createdAt: String): String {
        // 1) parse createdAt to Date
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = dateFormat.parse(createdAt) ?: return "이전"

        // 2) 오늘 0시 기준, 어제 0시, 7일 전 0시 등과 비교
        val now = Calendar.getInstance()
        val cal = Calendar.getInstance().apply { time = date }

        // 날짜 차이 계산
        val diffDays = ((now.timeInMillis - cal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

        return when {
            diffDays == 0 -> "오늘"
            diffDays == 1 -> "어제"
            diffDays in 2..6 -> "최근 7일"
            else -> "이전"
        }
    }


    fun checkAllNotificationRead() {
        // 모든 알림 읽음 처리 - patchAllNotificationRead API 호출
        viewLifecycleOwner.lifecycleScope.launch {
            val response = RetrofitClient.notificationService.patchAllNotificationRead()
            if (response?.isSuccess == true) {
                // 성공 시
                Log.d("NotificationFragment", "모든 알림 읽음 처리 성공")
            } else {
                // 실패 시
                // 실패 토스트 메시지 출력 - 로깅
                Log.d("NotificationFragment", "모든 알림 읽음 처리 실패")
            }
        }
    }

    fun onselectedListener() {
        binding.ivBack.setOnClickListener {
            // 뒤로가기 버튼 클릭 시 이전 화면으로 이동
           activity?.onBackPressed()
        }
        binding.ivNoti.setOnClickListener() {
            //  수신 설정 변경 - patchNotificationSettings API 호출
            viewLifecycleOwner.lifecycleScope.launch {
                val isBellOff = binding.ivNoti.drawable.constantState == ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_bell_off
                )?.constantState

                val request = NotificationSettingRequest(isBellOff)
                val response = RetrofitClient.notificationService.patchNotificationSettings(request)

                if (response?.isSuccess == true) {
                    val newIcon = if (response.result) R.drawable.ic_bell else R.drawable.ic_bell_off
                    binding.ivNoti.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            newIcon
                        )
                    )
                } else {
                    Toast.makeText(requireContext(), "알림 수신 설정 변경 실패", Toast.LENGTH_SHORT).show()


                }
            }
        }

    }

    private fun navigateToFragment(type: NotificationType) {
        // 예시로 Navigation Component를 사용했다고 가정
        when (type) {
            NotificationType.CHALLENGE_JOIN -> {
                // 챌린지 방 가입 -> 나의 챌린지 그룹 화면으로 이동
                findNavController().navigate(R.id.action_notificationFragment_to_chatFragment)
            }

            NotificationType.RANKING_REWARD -> {
                // 랭킹 리워드 지급 -> 나의 리워드 화면으로 이동
                findNavController().navigate(R.id.action_notificationFragment_to_rewardFragment)
            }

            NotificationType.NO_SPEND_REWARD -> {
                // 무지출 리워드 지급 -> 나의 리워드 화면
                findNavController().navigate(R.id.action_notificationFragment_to_rewardFragment)
            }

            NotificationType.DAILY_EXCEED -> {
                // 하루 지출 목표액 초과 -> 지출/일지 화면
                findNavController().navigate(R.id.action_notificationFragment_to_navigation_stat)
            }

            NotificationType.CHALLENGE_RANKING_UPDATE -> {
                // mm월 dd일 챌린지 그룹 랭킹 업데이트 -> 나의 챌린지 그룹 화면
                findNavController().navigate(R.id.action_notificationFragment_to_chatFragment)
            }
        }
    }
}
