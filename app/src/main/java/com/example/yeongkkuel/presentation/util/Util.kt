package com.example.yeongkkuel.presentation.util

import android.content.Context
import android.graphics.Paint
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.example.yeongkkuel.R
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
