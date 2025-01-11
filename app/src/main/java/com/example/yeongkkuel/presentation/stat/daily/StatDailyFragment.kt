package com.example.yeongkkuel.presentation.stat.daily

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentStatDailyBinding
import com.example.yeongkkuel.presentation.dpToPx
import com.example.yeongkkuel.presentation.statbotsheet.StatBotSheetCategoryListAdapter
import com.example.yeongkkuel.presentation.statbotsheet.StatBotSheetUiState
import com.example.yeongkkuel.presentation.statbotsheet.StatBotSheetViewModel
import com.example.yeongkkuel.presentation.statbotsheet.ViewPagerTouchListener
import com.example.yeongkkuel.presentation.toMoneyString
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatDailyFragment : Fragment() {
    private var _binding: FragmentStatDailyBinding? = null
    private val binding: FragmentStatDailyBinding
        get() = requireNotNull(_binding) { "FragmentStatBinding -> null" }

    private val viewModel: StatBotSheetViewModel by activityViewModels()
    private val statBotSheetCategoryListAdapter by lazy {
        StatBotSheetCategoryListAdapter()
    }

    private var viewPagerTouchListener: ViewPagerTouchListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is ViewPagerTouchListener) {
            viewPagerTouchListener = parentFragment as ViewPagerTouchListener
        }
    }

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
        fun initBottomSheet() {
            val bottomSheet = binding.clHomeItemBotSheet
            bottomSheet.visibility = View.VISIBLE
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

            val displayHeight = resources.displayMetrics.heightPixels
            val peekHeight =
                (displayHeight - 440.dpToPx(requireContext()))

            // BottomSheet의 초기 상태 설정
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            bottomSheetBehavior.peekHeight = peekHeight // 계산된 값 설정

            val height = displayHeight - (440 + 60).dpToPx(requireContext())
            rvBotSheetCategory.layoutParams.height = height
            rvBotSheetCategory.requestLayout() // 레이아웃 강제 갱신

            // BottomSheet 이벤트 핸들링
            bottomSheetBehavior.addBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            val height = displayHeight - (440 + 60).dpToPx(requireContext())
                            rvBotSheetCategory.layoutParams.height = height
                            rvBotSheetCategory.requestLayout() // 레이아웃 강제 갱신
                        }

                        BottomSheetBehavior.STATE_EXPANDED -> {
                            val height = displayHeight - 128.dpToPx(requireContext())
                            rvBotSheetCategory.layoutParams.height = height
                            rvBotSheetCategory.requestLayout() // 레이아웃 강제 갱신
                        }

                        else -> {
                            // 기타 상태 처리 (예: 드래그 상태 등)
                        }
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                }
            })

//            viewPagerTouchListener?.let { listener ->
//                bottomSheet.run {
//                    setOnTouchListener { _, event ->
//                        when (event.action) {
//                            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
//                                // BottomSheet가 터치될 때 ViewPager의 터치 이벤트를 막음
//                                listener.disableViewPagerTouch() // ViewPager 터치 비활성화
//                            }
//
//                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
//                                // BottomSheet의 터치가 끝나면 ViewPager 터치 이벤트 활성화
//                                listener.enableViewPagerTouch() // ViewPager 터치 활성화
//                            }
//                        }
//                        false // 터치 이벤트를 BottomSheet가 처리하도록 함
//                    }
//                }
//            }

            rvBotSheetCategory.run {
                // RecyclerView 터치 중에는 BottomSheet가 터치 이벤트를 받지 않도록 설정
                setOnTouchListener { _, _ ->
                    // RecyclerView가 터치될 때 BottomSheet가 드래그되지 않도록 설정
                    bottomSheetBehavior.isDraggable = false // BottomSheet 드래그 비활성화
                    bottomSheet.requestDisallowInterceptTouchEvent(true) // BottomSheet가 터치 이벤트를 받지 않도록 설정
                    false // RecyclerView의 터치 이벤트를 처리하도록 함
                }

                // RecyclerView 터치가 끝나면 BottomSheet가 다시 터치 가능한 상태로 복원
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            // RecyclerView가 스크롤이 멈추면 BottomSheet가 터치 가능하도록 복원
                            bottomSheet.requestDisallowInterceptTouchEvent(false) // 터치 이벤트 복원
                            bottomSheetBehavior.isDraggable = true // BottomSheet 드래그 활성화
                        }
                    }
                })
            }


        }


        fun initDate() {
            val currentDate = Date()
            val dateFormatChart = SimpleDateFormat("MM월 dd일 (E)", Locale.KOREAN)
            val formattedDateChart = dateFormatChart.format(currentDate)

            val dateFormatSheet = SimpleDateFormat("MM월 dd일 E요일", Locale.KOREAN)
            val formattedDateSheet = dateFormatSheet.format(currentDate)
            tvChartDate.text = formattedDateChart
            tvBottomSheetDate.text = formattedDateSheet
        }

        fun initRv() {
            rvBotSheetCategory.run {
                adapter = statBotSheetCategoryListAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
        }

        initBottomSheet()
        initDate()
        initRv()
    }

    private fun initViewModel() = with(viewModel) {
        viewLifecycleOwner.lifecycleScope.launch {
            uiState.flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collectLatest { uiState ->
                    onBind(uiState)
                }
        }
    }

    private fun onBind(uiState: StatBotSheetUiState) = with(binding) {
        fun initRvData() {
            statBotSheetCategoryListAdapter.submitList(uiState.spendingList)
        }

        fun initPieChart() {
            uiState.spendingList.let {
                val totalList = uiState.spendingList.map { spending ->
                    spending.history.sumOf { history -> history.price }
                }

                val othersTotal = uiState.total - totalList.sum()

                val otherTotalString = Math.abs(othersTotal).toMoneyString() + "원"
                tvChartTarget.text = otherTotalString
                if (othersTotal > 0) {
                    tvChartDescription.text = "하루 목표 지출액보다\n" +
                            "${otherTotalString}원 덜 썻어요!"
                } else {
                    tvChartDescription.text = "하루 목표 지출액보다\n" +
                            "${otherTotalString}원 더 썻어요!"
                }

                val pieChartDataList = ArrayList<PieEntry>().apply {
                    uiState.spendingList.forEach { spending ->
                        val totalPrice = spending.history.sumOf { it.price }
                        add(PieEntry(totalPrice.toFloat(), spending.kind))
                    }
                    add(PieEntry(othersTotal.toFloat(), "나머지"))
                }

                val colorList = uiState.spendingList.map {
                    ContextCompat.getColor(requireContext(), it.color)
                }.toMutableList()

                colorList.add(ContextCompat.getColor(requireContext(), R.color.black1)) // 색상 추가

                val dataSet = PieDataSet(pieChartDataList, "").apply {
                    colors = colorList // 색상 리스트 적용
                }


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
                    setTouchEnabled(false)  // 차트 터치 비활성화
                    setOnChartValueSelectedListener(null)  // 클릭 이벤트 리스너 제거
                    animate()
                }

                // Gson 객체 생성
                val gson = Gson()

                // 원본 데이터를 JSON 형식으로 직렬화
                val jsonString = gson.toJson(pieChartDataList)

                // JSON 형식의 데이터를 다시 역직렬화하여 리스트로 변환
                val typeToken = object : TypeToken<List<PieEntry>>() {}.type
                val copiedList = gson.fromJson<List<PieEntry>>(jsonString, typeToken)

                // 내림차순으로 정렬
                val sortedList = copiedList.sortedByDescending { it.value }
            }
        }

        initRvData()
        initPieChart()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}