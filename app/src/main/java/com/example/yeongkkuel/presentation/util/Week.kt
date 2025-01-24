package com.example.yeongkkuel.presentation.util

enum class Week(val kor: String) {
    SUN("일"), MON("월"), TUE("화"), WED("수"), THU("목"), FRI("금"), SAT("토"), ERROR("오류");

    companion object {
        fun getListItem() = listOf(
            Week.MON,
            Week.TUE,
            Week.WED,
            Week.THU,
            Week.FRI,
            Week.SAT,
            Week.SUN
        )

        fun fromString(day: String): Week {
            return when (day) {
                "Sunday" -> SUN
                "Monday" -> MON
                "Tuesday" -> TUE
                "Wednesday" -> WED
                "Thursday" -> THU
                "Friday" -> FRI
                "Saturday" -> SAT
                else -> ERROR
            }
        }
    }
}