package com.example.yeongkkuel.presentation.util

import android.content.Context
import android.text.Editable
import java.text.NumberFormat
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

