package com.example.yeongkkuel.presentation.chat.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentFilterExpenseDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kr.co.prnd.slider.FlexibleStepRangeSlider

class FilterExpenseDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentFilterExpenseDialogBinding? = null
    private val binding: FragmentFilterExpenseDialogBinding
        get() = requireNotNull(_binding){"FragmentFilterExpenseDialogBinding -> null"}

    private val viewModel: ChatSearchViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterExpenseDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rangeSlider.setValues(
            values = (0..100000 step 5000).map { it.toFloat() },
            valueFrom = 0f,
            valueTo = 10000f
        )

        binding.rangeSlider.addOnValueChangeListener { from, to, state ->
            when (state) {
                FlexibleStepRangeSlider.ValueChangeState.Dragging -> {
                    // 드래그 중: 실시간으로 값을 업데이트 (예: UI에 표시)
                    updateDisplay(from, to)
                }
                FlexibleStepRangeSlider.ValueChangeState.Idle -> {
                    // 슬라이더에서 손을 뗐을 때: 스냅하여 고정된 값으로 보정
                    val snappedFrom = snapValue(from)
                    val snappedTo = snapValue(to)
                    updateDisplay(snappedFrom, snappedTo)
                    viewModel.selectedMinExpenseOption.value = snappedFrom.toInt()
                    viewModel.selectedMaxExpenseOption.value = snappedTo.toInt()
                }
            }
        }
    }

    // 고정 간격에 맞게 스냅(snap)하는 함수
    private fun snapValue(value: Float): Float {
        val steps = (0..100000 step 5000).map { it.toFloat() }
        return steps.minByOrNull { kotlin.math.abs(it - value) } ?: value
    }

    private fun updateDisplay(from: Float, to: Float) {
        binding.etDailyGoalExpenseMin.setText(from.toInt().toString())
        binding.etDailyGoalExpenseMax.setText(
            if (to.toInt() == 100000) "100000+" else to.toInt().toString()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
