package com.example.yeongkkuel.presentation.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentChatRoomRankBinding
import com.google.android.material.tabs.TabLayout

class ChatRoomRankFragment : Fragment() {

    private lateinit var navController: NavController
    private var _binding: FragmentChatRoomRankBinding? = null
    private val binding: FragmentChatRoomRankBinding
        get() = requireNotNull(_binding){"FragmentChatRoomRankBinding -> null"}

    // 현재 선택된 탭의 인덱스를 저장하기 위한 키
    companion object {
        private const val SELECTED_TAB_INDEX = "selected_tab_index"
    }

    // 탭의 인덱스를 저장하는 변수
    private var selectedTabIndex: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentChatRoomRankBinding.inflate(inflater, container, false)

        // 이전 상태에서 선택된 탭 인덱스 복원
        savedInstanceState?.let {
            selectedTabIndex = it.getInt(SELECTED_TAB_INDEX, 0)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        val tabLayout: TabLayout = binding.tabLayout

        // 탭 선택 리스너 설정
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                selectedTabIndex = tab.position
                when (tab.position) {
                    0 -> replaceFragment(ChatRoomRankDailyFragment())
                    1 -> replaceFragment(ChatRoomRankWeeklyFragment())
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // savedInstanceState가 null이 아니면 선택된 탭 복원
        if (savedInstanceState == null) {
            // 초기 탭 설정
            tabLayout.getTabAt(selectedTabIndex)?.select()
            replaceFragment(
                if (selectedTabIndex == 0) ChatRoomRankDailyFragment()
                else ChatRoomRankWeeklyFragment()
            )
        } else {
            // 선택된 탭 복원
            tabLayout.getTabAt(selectedTabIndex)?.select()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        childFragmentManager.commit {
            replace(R.id.record_fragment_Container, fragment)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 선택된 탭 인덱스를 저장
        outState.putInt(SELECTED_TAB_INDEX, selectedTabIndex)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}