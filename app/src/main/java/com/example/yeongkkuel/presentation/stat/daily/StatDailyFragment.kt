package com.example.yeongkkuel.presentation.stat.daily

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentStatBinding
import com.example.yeongkkuel.databinding.FragmentStatDailyBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StatDailyFragment : Fragment() {
    private var _binding: FragmentStatDailyBinding? = null
    private val binding: FragmentStatDailyBinding
        get() = requireNotNull(_binding) { "FragmentStatBinding -> null" }

    private val viewModel: StatDailyViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStatDailyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initViewModel()
    }

    private fun initView() = with(binding) {
        fun initBottomSheet(){
            val bottomSheet = binding.clHomeItemBotSheet
            bottomSheet.visibility = View.VISIBLE
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

            val displayHeight = resources.displayMetrics.heightPixels
            val peekHeight = (displayHeight - resources.getDimensionPixelSize(R.dimen.space_440dp)) // 244dp 빼기

            // BottomSheet의 초기 상태 설정
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            bottomSheetBehavior.peekHeight = peekHeight // 계산된 값 설정

            // BottomSheet 이벤트 핸들링
            bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    // 상태 변화에 따른 처리
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    // 슬라이딩 중에 필요한 처리
                }
            })
        }

        initBottomSheet()
    }

    private fun initViewModel() = with(viewModel) {
        viewLifecycleOwner.lifecycleScope.launch {
            uiState.flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collectLatest { uiState ->
                    onBind(uiState)
                }
        }
    }

    private fun onBind(uiState: StatDailyUiState) = with(binding) {

        // pie chart 적용
        uiState.chartList.let{ dataList ->
            val dataSet = PieDataSet(dataList, "")

            dataSet.colors = listOf(
                ContextCompat.getColor(requireContext(), R.color.black1),
                ContextCompat.getColor(requireContext(), R.color.pink),
                ContextCompat.getColor(requireContext(), R.color.main1),
                )

            dataSet.valueTextSize = 16F
            dataSet.setDrawValues(false) // value 비활성화

            val pieData = PieData(dataSet)

            pieChart.apply {
                data = pieData
                description.isEnabled = false // 차트 설명 비활성화
                legend.isEnabled = false // 하단 설명 비활성화
                isRotationEnabled = true // 차트 회전 활성화
                setDrawEntryLabels(false) // 엔트리 라벨 비활성화
                setEntryLabelColor(Color.BLACK) // label 색상
                animateY(1400, Easing.EaseInOutQuad) // 1.4초 동안 애니메이션 설정
                animate()
            }

            // Gson 객체 생성
            val gson = Gson()

            // 원본 데이터를 JSON 형식으로 직렬화
            val jsonString = gson.toJson(dataList)

            // JSON 형식의 데이터를 다시 역직렬화하여 리스트로 변환
            val typeToken = object : TypeToken<List<PieEntry>>() {}.type
            val copiedList = gson.fromJson<List<PieEntry>>(jsonString, typeToken)

            // 내림차순으로 정렬
            val sortedList = copiedList.sortedByDescending { it.value }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}