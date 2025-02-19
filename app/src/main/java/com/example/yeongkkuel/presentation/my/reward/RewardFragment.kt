package com.example.yeongkkuel.presentation.my.reward

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yeongkkuel.databinding.FragmentRewardBinding
import com.example.yeongkkuel.network.RetrofitClient
import com.example.yeongkkuel.presentation.my.reward.data.RewardItem
import com.example.yeongkkuel.presentation.my.reward.data.RewardListItem
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


        rewardAdapter = RewardAdapter(emptyList())

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
                        val detailList = body
                        val itemList = detailList.map {
                            RewardMapper.mapToRewardItem(it)
                        }
                        val dummyData : List<RewardItem> = getDummyRewardItem()

                        if(itemList.isEmpty()){
                            val finalDisplayList = toDisplayList(dummyData)
                            rewardAdapter = RewardAdapter(finalDisplayList)
                            binding.rvReward.adapter = rewardAdapter
                        } else {
                            val finalDisplayList = toDisplayList(itemList)
                            rewardAdapter = RewardAdapter(finalDisplayList)
                            binding.rvReward.adapter = rewardAdapter
                        }
                    }
                } else {
                    Log.d("NotificationFragment", "알림 목록 가져오기 실패")
                }
            } catch (e: Exception) {
                // 네트워크 오류, JSON 파싱 오류 등
                e.printStackTrace()
            }
        }
    }

    private fun toDisplayList(originalItems: List<RewardItem>): List<RewardListItem> {
        // 1) 섹션별 그룹화
        val groupedMap = originalItems.groupBy { it.section }

        // 2) 원하는 섹션 표시 순서 정의
        val sectionOrder = listOf("오늘", "어제", "최근 7일")

        // 3) 최종 표시 리스트 구성
        val result = mutableListOf<RewardListItem>()
        sectionOrder.forEach { sectionName ->
            val itemsInSection = groupedMap[sectionName]
            if (!itemsInSection.isNullOrEmpty()) {
                // -- 헤더 추가 --
                result.add(RewardListItem.HeaderItem(sectionName))
                // -- 섹션 내 아이템들 추가 --
                for (reward in itemsInSection) {
                    result.add(RewardListItem.NormalItem(reward))
                }
            }
        }
        return result
    }

    private fun getDummyRewardItem(): List<RewardItem> {

        val dummyRewardItem = mutableListOf<RewardItem>()
        dummyRewardItem.add(RewardItem(RewardType.CHALLENGE_JOIN, "챌린지 참여", "1000", "오늘"))
        dummyRewardItem.add(RewardItem(RewardType.RANKING_REWARD, "랭킹 리워드", "2000", "어제"))
        dummyRewardItem.add(RewardItem(RewardType.NO_SPEND_REWARD, "지출 없음 리워드", "3000", "어제"))
        dummyRewardItem.add(RewardItem(RewardType.DAILY_EXCEED, "일일 초과", "4000", "어제"))
        dummyRewardItem.add(RewardItem(RewardType.CHALLENGE_RANKING_UPDATE, "챌린지 랭킹 업데이트", "5000", "최근 7일"))
        return dummyRewardItem
    }





}