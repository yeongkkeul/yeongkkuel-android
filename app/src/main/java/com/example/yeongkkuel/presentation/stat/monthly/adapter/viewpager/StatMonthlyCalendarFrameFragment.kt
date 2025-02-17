package com.example.yeongkkuel.presentation.stat.monthly.adapter.viewpager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.yeongkkuel.databinding.ItemCalenderFrameBinding
import com.example.yeongkkuel.presentation.botsheet.BotSheetViewModel
import com.example.yeongkkuel.presentation.stat.monthly.StatMonthlyUiState
import com.example.yeongkkuel.presentation.stat.monthly.StatMonthlyViewModel
import com.example.yeongkkuel.presentation.stat.monthly.adapter.StatMonthlyCalendarListAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

class StatMonthlyCalendarFrameFragment(
    private val viewModel: StatMonthlyViewModel,
    private val minusMonth: Int
) : Fragment() {
    private var _binding: ItemCalenderFrameBinding? = null
    private val binding: ItemCalenderFrameBinding
        get() = requireNotNull(_binding) { "StatMonthlyCalendarFrameFragment -> null" }

    private val botViewModel: BotSheetViewModel by activityViewModels()

    private val listAdapter by lazy {
        StatMonthlyCalendarListAdapter(botViewModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ItemCalenderFrameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initViewModel()
    }

    private fun initView() = with(binding) {
        fun initRv() {
            rvCalendarFrame.run {
                adapter = listAdapter
                layoutManager = GridLayoutManager(requireContext(), 7)
            }
        }

        initRv()
    }


    private fun getCalendar() {
        val calendar = Calendar.getInstance()

        // 현재 날짜에서 minusMonth 만큼 이전으로 이동
        calendar.add(Calendar.MONTH, -minusMonth)

        // 연도와 월을 가져오기 (월은 0부터 시작하므로 +1 필요)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        viewModel.getCalender(year = year, month = month)
    }

    private fun initViewModel() = with(viewModel) {
        viewLifecycleOwner.lifecycleScope.launch {
            uiState.flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collectLatest { uiState ->
                    onBind(uiState)
                }
        }
    }

    override fun onResume() {
        getCalendar()
        super.onResume()
    }

    private fun onBind(uiState: StatMonthlyUiState) = with(binding) {
        if(uiState is StatMonthlyUiState.StatMonthly) listAdapter.submitList(uiState.calendarList)
    }

    fun setTargetExpenditure(targetExpenditure: Int?){
        listAdapter.setTargetExpenditure(targetExpenditure)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}