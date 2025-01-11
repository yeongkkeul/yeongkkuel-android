package com.example.yeongkkuel.presentation.stat

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentStatBinding
import com.example.yeongkkuel.presentation.statbotsheet.ViewPagerTouchListener
import com.google.android.material.tabs.TabLayoutMediator

class StatFragment : Fragment(), ViewPagerTouchListener {
    private lateinit var navController: NavController
    private var _binding: FragmentStatBinding? = null
    private val binding: FragmentStatBinding
        get() = requireNotNull(_binding) { "FragmentStatBinding -> null" }

    private val viewPagerAdapter: StatViewPagerAdapter by lazy {
        StatViewPagerAdapter(this@StatFragment)
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
        vpStat.adapter = viewPagerAdapter
        vpStat.offscreenPageLimit = viewPagerAdapter.itemCount

        TabLayoutMediator(tlStat, vpStat) { tab, position ->
            val tabView = TextView(context).apply {
                setText(viewPagerAdapter.getTitle(position))
                setTextAppearance(R.style.body_semibo) // 스타일 적용
                setTextColor(ContextCompat.getColor(context, R.color.main1))
                gravity = Gravity.CENTER
            }
            tab.customView = tabView
        }.attach()
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