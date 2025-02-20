package com.example.yeongkkuel.presentation.login.tutorial

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentTutorialBinding
import com.example.yeongkkuel.presentation.login.tutorial.data.OnboardingItem

class TutorialFragment : Fragment() {

    private var _binding: FragmentTutorialBinding? = null
    private val binding get() = _binding!!

    val showRewardModal = arguments?.getBoolean("showRewardModal") ?: false

    private val onboardingItems = listOf(
        OnboardingItem(
            imageRes = R.drawable.img_tutorial_1,
            title = "돈 절약을 게임하듯이!\n" +
                    "저축할수록\n" +
                    "풍성해지는 나의 캐릭터",
            description = "절약 챌린지 성공으로 받은 리워드로\n" +
                    "나의 캐릭터를 꾸며봐요."
        ),
        OnboardingItem(
            imageRes = R.drawable.img_tutorial_2,
            title = "나에게 딱 맞는\n" +
                    "페이스 메이커를 찾아봐요!",
            description = "나와 비슷한 사람들과 지출 내역을 공유하며\n" +
                    "잔소리, 응원을 들을 수 있어요."
        ),
        OnboardingItem(
            imageRes = R.drawable.img_tutorial_3,
            title = "격려하고 자극하며\n" +
                    "함께해서 더 재밌는\n" +
                    "무지출 챌린지",
            description = "내가 기입한 지출 내역이 자동으로\n" +
                    "참여 중인 챌린지 채팅방에 전송돼요."
        ),
        OnboardingItem(
            imageRes = R.drawable.img_tutorial_4,
            title = "하루 지출 내역으로\n" +
                    "매일 업데이트 되는\n" +
                    "절약 점수 랭킹",
            description = "채팅방 챌린저들끼리, 다른 채팅방끼리\n" +
                    "매일 절약 점수와 랭킹을 매기며 동기 부여를 얻어요."
        ),
        OnboardingItem(
            imageRes = R.drawable.img_tutorial_5,
            title = "나와 같은 연령대,\n" +
                    "직업을 가진 사람들 중 나는\n" +
                    "얼마나 절역하고 있을까?",
            description = "일간, 주간, 월간 지출 데이터를\n" +
                    "한눈에, 쉽고 직관적으로 보여줄게요."
        )
    )



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTutorialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 2) ViewPager2 어댑터 연결
        val onboardingAdapter = OnboardingAdapter(onboardingItems)
        binding.viewPager.adapter = onboardingAdapter

        // 3) WormDotsIndicator와 연결
        //    라이브러리 메서드: setViewPager2(viewPager2)
        binding.dotsIndicator.setViewPager2(binding.viewPager)

        // 4) '시작하기' 버튼 클릭 시 → 예: MainActivity로 이동
        binding.btnStart.setOnClickListener {
            gotohome()
        }
    }

    private fun gotohome() {
        val bundle = Bundle()
        bundle.putBoolean("showRewardModal", showRewardModal)
        findNavController().navigate(R.id.action_tutorialFragment_to_navigation_home, bundle)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }




}