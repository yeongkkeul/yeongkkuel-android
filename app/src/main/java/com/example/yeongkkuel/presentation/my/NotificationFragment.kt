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
import com.example.yeongkkuel.presentation.my.notification.NotificationAdapter
import com.example.yeongkkuel.presentation.my.notification.data.NotificationItem
import com.example.yeongkkuel.presentation.my.notification.data.NotificationType
import kotlinx.coroutines.launch


class NotificationFragment : Fragment() {

    private lateinit var binding: FragmentNotificationBinding  // viewBinding 사용하는 경우


    // 예시 알림 데이터
    private val notificationList = listOf(
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
        checkAllNotificationRead()
        val adapter = NotificationAdapter(notificationList) { clickedItem ->
            navigateToFragment(clickedItem.type)
        }

        binding.rvNotification.adapter = adapter
        binding.rvNotification.layoutManager = LinearLayoutManager(requireContext())

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
            findNavController().popBackStack()

        }
        binding.ivNoti.setOnClickListener() {
            //  수신 설정 변경 - patchNotificationSettings API 호출
            viewLifecycleOwner.lifecycleScope.launch {
                if (binding.ivNoti.drawable.constantState == ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_bell_off
                    )?.constantState
                ) {
                    // 현재 알림 수신 설정이 꺼져있는 경우
                    val response =
                        RetrofitClient.notificationService.patchNotificationSettings(true)
                    if (response?.isSuccess == true) {
                        // 성공 시
                        binding.ivNoti.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_bell
                            )
                        )
                    } else {
                        // 실패 시
                        // 실패 토스트 메시지 출력
                        Toast.makeText(requireContext(), "알림 수신 설정 변경 실패", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    // 현재 알림 수신 설정이 켜져있는 경우
                    val response =
                        RetrofitClient.notificationService.patchNotificationSettings(false)
                    if (response?.isSuccess == true) {
                        // 성공 시
                        binding.ivNoti.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_bell_off
                            )
                        )
                    } else {
                        // 실패 시
                        // 실패 토스트 메시지 출력
                        Toast.makeText(requireContext(), "알림 수신 설정 변경 실패", Toast.LENGTH_SHORT)
                            .show()
                    }
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
