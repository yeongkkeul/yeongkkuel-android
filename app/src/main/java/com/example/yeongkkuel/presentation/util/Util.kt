package com.example.yeongkkuel.presentation.util

import android.content.Context
import android.text.Editable
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

fun Int.toMoneyString(): String {
    return NumberFormat.getNumberInstance(Locale.US).format(this)
}

fun Int.dpToPx(context: Context): Int {
    return (this * context.resources.displayMetrics.density).toInt()
}

fun String.clearComma(): Int? = this.replace(",", "").toIntOrNull()

fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

fun String.getDay(): Int = this.split("-").last().toInt() // 날짜 형식이 yyyy-mm-dd 형식의 string

fun getDayOfWeekNum(date: LocalDate): Int {
    val dayString = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN) // 예: "월요일"
    return when (dayString) {
        "월요일" -> 1
        "화요일" -> 2
        "수요일" -> 3
        "목요일" -> 4
        "금요일" -> 5
        "토요일" -> 6
        "일요일" -> 7
        else -> 0
    }
}