package com.example.yeongkkuel.presentation.stat

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentStatBinding
import com.example.yeongkkuel.presentation.botsheet.BotSheetListener
import com.example.yeongkkuel.presentation.botsheet.BotSheetViewModel
import com.example.yeongkkuel.presentation.stat.monthly.StatMonthlyViewModel
import com.example.yeongkkuel.presentation.stat.weekly.StatWeeklyViewModel
import com.example.yeongkkuel.presentation.util.dpToPx
import com.google.android.material.tabs.TabLayoutMediator

class StatFragment : Fragment(), ViewPagerTouchListener {
    private lateinit var navController: NavController
    private var _binding: FragmentStatBinding? = null
    private val binding: FragmentStatBinding
        get() = requireNotNull(_binding) { "FragmentStatBinding -> null" }

    private val weeklyViewModel: StatWeeklyViewModel by viewModels()
    private val monthlyViewModel: StatMonthlyViewModel by viewModels()
    private val botSheetViewModel : BotSheetViewModel by activityViewModels()

    private val viewPagerAdapter: StatViewPagerAdapter by lazy {
        StatViewPagerAdapter(
            fragment = this@StatFragment,
            weeklyViewModel = weeklyViewModel,
            monthlyViewModel = monthlyViewModel
        )
    }

    private var botSheetListener: BotSheetListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is BotSheetListener) {
            botSheetListener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentStatBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        initView()
    }

    private fun initView() = with(binding) {
        fun initVp() = with(vpStat) {
            vpStat.adapter = viewPagerAdapter
            vpStat.offscreenPageLimit = viewPagerAdapter.itemCount

            // 전달된 Bundle로부터 선택된 탭 인덱스를 가져옴
            val selectedTabIndex = arguments?.getInt("selected_tab_index") ?: 0
            vpStat.setCurrentItem(selectedTabIndex, false) // 해당 탭 선택

            TabLayoutMediator(tlStat, vpStat) { tab, position ->
                val tabView = TextView(context).apply {
                    setText(viewPagerAdapter.getTitle(position))
                    setTextAppearance(R.style.body_semibo)
                    setTextColor(ContextCompat.getColorStateList(context, R.color.tab_stat_text))
                    gravity = Gravity.CENTER
                }
                tab.customView = tabView
            }.attach()


            // ViewPager2의 페이지가 변경될 때마다 호출되는 콜백
            botSheetListener?.let { listner ->
                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)

                        (adapter as StatViewPagerAdapter).animate(position)

                        when (position) {
                            0 -> { // 첫 번째 페이지 (StatDailyFragment)
                                listner.setBotSheetVisible()

                                val displayHeight = resources.displayMetrics.heightPixels
                                val peekHeight =
                                    (displayHeight - 430.dpToPx(requireContext()))
                                listner.setPeekHeight(peekHeight)

                                botSheetViewModel.getSpendingList()
                            }

                            1 -> { // 두 번째 페이지 (StatWeeklyFragment)
                                listner.setBotSheetGone()
                            }

                            2 -> { // 세 번째 페이지 (StatMonthlyFragment)
                                listner.setBotSheetVisible()

                                val displayHeight = resources.displayMetrics.heightPixels
                                val peekHeight =
                                    (displayHeight - 528.dpToPx(requireContext()))
                                listner.setPeekHeight(peekHeight)
                            }
                        }
                    }
                })
            }
        }

        fun initMore() {
            includeTopbar.ivMore.setOnClickListener {
                if (clMore.visibility == View.GONE) clMore.visibility = View.VISIBLE
                else clMore.visibility = View.GONE
            }

            tvMoreSettings.setOnClickListener { findNavController().navigate(R.id.navigation_stat_setting) }

            tvMoreRecommendation.setOnClickListener { findNavController().navigate(R.id.navigation_stat_recommendation) }

        }

        initVp()
        initMore()
    }

    // ViewPager의 터치 이벤트를 비활성화하는 함수
    override fun disableViewPagerTouch() {
        binding.vpStat.isUserInputEnabled = false // 터치 비활성화
    }

    // ViewPager의 터치 이벤트를 활성화하는 함수
    override fun enableViewPagerTouch() {
        binding.vpStat.isUserInputEnabled = true // 터치 활성화
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}