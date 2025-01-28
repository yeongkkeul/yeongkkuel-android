package com.example.yeongkkuel.presentation.my.reward

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentNotificationBinding
import com.example.yeongkkuel.databinding.FragmentRewardBinding
import com.example.yeongkkuel.presentation.my.notification.NotificationAdapter
import com.example.yeongkkuel.presentation.my.notification.data.NotificationItem
import com.example.yeongkkuel.presentation.my.notification.data.NotificationType
import com.example.yeongkkuel.presentation.my.reward.data.RewardItem
import com.example.yeongkkuel.presentation.my.reward.data.RewardType


class RewardFragment : Fragment() {

    private lateinit var binding: FragmentRewardBinding  // viewBinding 사용하는 경우


    // 리워드 더미 데이터
    private val rewardList = listOf(
        RewardItem(
            RewardType.CHALLENGE_JOIN,
            "무지출이 대세다 방 가입 완료",
            "30",
            "오늘"
        ),
        RewardItem(
            RewardType.NO_SPEND_REWARD,
            "돈 모아서 차 사자 방 가입 완료",
            "30",
            "오늘"
        ),
        RewardItem(
            RewardType.RANKING_REWARD,
            "랭킹 리워드 지급",
            "30",
            "오늘"
        ),
        RewardItem(
            RewardType.DAILY_EXCEED,
            "하루 지출 목표액 초과",
            "20",
            "어제"
        ),
        RewardItem(
            RewardType.NO_SPEND_REWARD,
            "무지출 리워드 지급",
            "20",
            "어제"
        ),
        RewardItem(
            RewardType.CHALLENGE_RANKING_UPDATE,
            "12월 31일 챌린지 그룹 랭킹 업데이트",
            "30",
            "어제"
        ),
        RewardItem(
            RewardType.CHALLENGE_JOIN,
            "무지출이 대세다 방 가입 완료",
            "12/12",
            "최근 7일"
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRewardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뒤로가기 버튼 클릭 시 이전 화면으로 이동
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // RecyclerView 설정
        val adapter = RewardAdapter(rewardList) { clickedItem ->
            // 아이템 클릭 시 이동 로직
//            navigateToFragment(clickedItem.type)

        }
        binding.rvReward.adapter = adapter
        binding.rvReward.layoutManager = LinearLayoutManager(requireContext())


        /*// 구분선 적용
        fun dpToPx(dp: Int): Int =
            (dp * resources.displayMetrics.density).toInt()

        val decoration = SectionDividerItemDecoration(
            adapter = adapter,
            dividerHeight = dpToPx(12).toFloat(),
            dividerColor = ContextCompat.getColor(requireContext(), R.color.black0),
            gapBetweenItemAndDivider = dpToPx(24),
            gapBetweenHeaderAndDivider = dpToPx(24)
        )
        binding.rvNotification.addItemDecoration(decoration)*/

    }

//    private fun navigateToFragment(type: NotificationType) {
//        // 예시로 Navigation Component를 사용했다고 가정
//        when (type) {
//            NotificationType.CHALLENGE_JOIN -> {
//                // 챌린지 방 가입 -> 나의 챌린지 그룹 화면으로 이동
//                findNavController().navigate(R.id.action_notificationFragment_to_myChallengeGroupFragment)
//            }
//            NotificationType.RANKING_REWARD -> {
//                // 랭킹 리워드 지급 -> 나의 리워드 화면으로 이동
//                findNavController().navigate(R.id.action_notificationFragment_to_myRewardFragment)
//            }
//            NotificationType.NO_SPEND_REWARD -> {
//                // 무지출 리워드 지급 -> 나의 리워드 화면
//                findNavController().navigate(R.id.action_notificationFragment_to_myRewardFragment)
//            }
//            NotificationType.DAILY_EXCEED -> {
//                // 하루 지출 목표액 초과 -> 지출/일지 화면
//                findNavController().navigate(R.id.action_notificationFragment_to_spendDiaryFragment)
//            }
//            NotificationType.CHALLENGE_RANKING_UPDATE -> {
//                // mm월 dd일 챌린지 그룹 랭킹 업데이트 -> 나의 챌린지 그룹 화면
//                findNavController().navigate(R.id.action_notificationFragment_to_myChallengeGroupFragment)
//            }
//        }
//    }
}