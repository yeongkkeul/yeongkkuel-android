package com.example.yeongkkuel.presentation.util

import android.content.Context
import java.text.NumberFormat
import java.util.Locale

fun Int.toMoneyString(): String {
    return NumberFormat.getNumberInstance(Locale.US).format(this)
}

fun Int.dpToPx(context: Context): Int {
    return (this * context.resources.displayMetrics.density).toInt()
}