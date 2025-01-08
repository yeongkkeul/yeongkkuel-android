package com.example.yeongkkuel.presentation

import java.text.NumberFormat
import java.util.Locale

fun Int.toMoneyString(): String {
    return NumberFormat.getNumberInstance(Locale.US).format(this)
}