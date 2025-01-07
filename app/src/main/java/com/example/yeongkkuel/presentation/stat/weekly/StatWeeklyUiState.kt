package com.example.yeongkkuel.presentation.stat.weekly

import com.github.mikephil.charting.data.Entry

data class StatWeeklyUiState(
    val charEntryList: List<Entry>
) {
    companion object {
        fun init() = StatWeeklyUiState(
            charEntryList = listOf(
                Entry(0f, 10f),
                Entry(1f, 20f),
                Entry(2f, 15f),
                Entry(3f, 30f),
//                Entry(4f, 10f),
//                Entry(5f, 20f),
//                Entry(6f, 15f),
            )
        )
    }
}
