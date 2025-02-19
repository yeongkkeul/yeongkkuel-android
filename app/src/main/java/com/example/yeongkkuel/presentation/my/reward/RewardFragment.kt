package com.example.yeongkkuel.presentation.my.reward

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentRewardBinding
import com.example.yeongkkuel.network.RetrofitClient
import com.example.yeongkkuel.presentation.my.notification.data.NotificationType
import com.example.yeongkkuel.presentation.my.reward.data.RewardItem
import com.example.yeongkkuel.presentation.my.reward.data.RewardType
import kotlinx.coroutines.launch


class RewardFragment : Fragment() {

    private lateinit var binding: FragmentRewardBinding  // viewBinding 사용하는 경우

    private lateinit var rewardAdapter: RewardAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRewardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView 설정
        rewardAdapter = RewardAdapter(emptyList()) { clickedItem ->
            // 아이템 클릭 시 이동 로직
//            navigateToFragment(clickedItem.type)
        }

        binding.apply {
            rvReward.adapter = rewardAdapter
            rvReward.layoutManager = LinearLayoutManager(requireContext())
            ivBack.setOnClickListener {
                findNavController().popBackStack()
            }
        }


        fetchRewardsFromServer()


    }

    private fun fetchRewardsFromServer() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.myPageService.getRewards()
                if (response.isSuccess) {
                    val body = response.result
                    if (body != null) {
                        // 서버에서 받아온 result -> RewardItem 변환
                        val rewardItems: List<RewardItem> = body.map {
                            RewardMapper.mapToRewardItem(it)
                        }

                        // 새로 받은 리스트로 갱신
                        // 더미데이터
                        val dummyData = listOf(
                            RewardItem(RewardType.CHALLENGE_JOIN, "챌린지 참여", "100 포인트", "오늘"),
                            RewardItem(RewardType.RANKING_REWARD, "랭킹 리워드", "200 포인트", "어제"),
                            RewardItem(RewardType.DAILY_EXCEED, "하루 지출 초과", "50 포인트", "최근 7일"),
                            RewardItem(RewardType.CHALLENGE_RANKING_UPDATE, "챌린지 랭킹 업데이트", "150 포인트", "최근 7일")
                        )

                        val combinedItems =  dummyData

                        updateRewardList(combinedItems)
                    } else {
                        // isSuccess가 false거나 body가 null인 경우 처리
                    }
                } else {
                    // response.isSuccessful이 아닌 경우(4xx, 5xx 에러 등)
                }
            } catch (e: Exception) {
                // 네트워크 오류, JSON 파싱 오류 등
            }
        }
    }

    private fun updateRewardList(newItems: List<RewardItem>) {
        // RewardAdapter를 다시 생성하는 대신,
        rewardAdapter.updateItems(newItems)
    }
}