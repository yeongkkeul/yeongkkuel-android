package com.example.yeongkkuel.presentation.stat.daily

import com.github.mikephil.charting.data.PieEntry

data class StatDailyUiState(
    val chartList: List<PieEntry>
){
    companion object{
        fun init() = StatDailyUiState(
            chartList = listOf(
                PieEntry(0.5f, "나머지"),
                PieEntry(0.5f, "사용량"),
            )
        )
    }
}