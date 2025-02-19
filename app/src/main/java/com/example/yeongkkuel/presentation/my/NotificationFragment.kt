package com.example.yeongkkuel.presentation.my

import android.content.Context
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
import com.example.yeongkkuel.presentation.my.notification.data.NotificationListItem
import com.example.yeongkkuel.presentation.my.notification.data.NotificationType
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class NotificationFragment : Fragment() {

    private val PREFS_NAME = "my_prefs"
    private val KEY_NOTIFICATION_ON = "is_notification_on"

    private var isNotificationOn = false

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

        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // 1. SharedPreferences 에서 저장된 알림 상태 불러오기 (기본값 false)
        isNotificationOn = prefs.getBoolean(KEY_NOTIFICATION_ON, false)

        // 2. 불러온 상태에 따라 아이콘 초기화
        val newIcon = if (isNotificationOn) R.drawable.ic_bell else R.drawable.ic_bell_off

        binding.ivNoti.setImageResource(newIcon)

        binding.rvNotification.layoutManager = LinearLayoutManager(requireContext())



        onselectedListener()
//        checkAllNotificationRead()
        adapter = NotificationAdapter(
            emptyList()
        ) { clickedItem ->
            navigateToFragment(clickedItem.type)
        }
        binding.rvNotification.adapter = adapter
//        binding.rvNotification.layoutManager = LinearLayoutManager(requireContext())
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
                        val dummyData : List<NotificationItem> = getDummyNotifications()
                        // notificationItem 으로 변환
                        val itemList = detailList.map { mapToNotificationItem(it) }


                        if(itemList.isEmpty()){
                            val finalDisplayList = toDisplayList(dummyData)
                            adapter = NotificationAdapter(finalDisplayList) { clickedItem ->
                                navigateToFragment(clickedItem.type)
                            }
                            binding.rvNotification.adapter = adapter
                        } else {
                            val finalDisplayList = toDisplayList(itemList)
                            adapter = NotificationAdapter(finalDisplayList) { clickedItem ->
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

    // "오늘", "어제", "최근 7일" 순서대로 섹션화
    private fun toDisplayList(originalItems: List<NotificationItem>): List<NotificationListItem> {
        // 1) 섹션별 그룹화
        val groupedMap = originalItems.groupBy { it.section }

        // 2) 원하는 섹션 표시 순서 정의
        val sectionOrder = listOf("오" +
                "늘", "어제", "최근 7일")

        // 3) 최종 표시 리스트 구성
        val result = mutableListOf<NotificationListItem>()
        sectionOrder.forEach { sectionName ->
            val itemsInSection = groupedMap[sectionName]
            if (!itemsInSection.isNullOrEmpty()) {
                // -- 헤더 추가 --
                result.add(NotificationListItem.HeaderItem(sectionName))
                // -- 섹션 내 아이템들 추가 --
                for (noti in itemsInSection) {
                    result.add(NotificationListItem.NormalItem(noti))
                }
            }
        }
        return result
    }

    private fun getDummyNotifications(): List<NotificationItem> {
        return listOf(
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
            ),
            // 일부러 "오늘" 섹션 하나 더
            NotificationItem(
                NotificationType.NO_SPEND_REWARD,
                "아이템 중복 예시",
                "어딘가 시간",
                "오늘"
            )
        )
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

        binding.ivNoti.setOnClickListener {
            val newSetting = !isNotificationOn
            val request = NotificationSettingRequest(newSetting)

            viewLifecycleOwner.lifecycleScope.launch {
                // API 호출
                val response = notificationService.patchNotificationSettings(request)
                if (response?.isSuccess == true) {
                    // 서버에서 온 결과로 최종 상태를 갱신
                    isNotificationOn = response.result

                    // UI 아이콘 바꾸기
                    val updatedIcon = if (isNotificationOn) R.drawable.ic_bell else R.drawable.ic_bell_off
                    binding.ivNoti.setImageResource(updatedIcon)


                    // SharedPreferences 에도 갱신된 상태 저장
                    val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    prefs.edit().putBoolean(KEY_NOTIFICATION_ON, isNotificationOn).apply()

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
